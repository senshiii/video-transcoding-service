package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.db.model.MediaVideo;
import me.sayandas.db.model.Message;
import me.sayandas.db.repository.MediaVideoRepository;
import me.sayandas.db.repository.MessageRepository;
import me.sayandas.utils.LogUtils;
import me.sayandas.utils.S3Utils;
import me.sayandas.utils.SQSUtil;
import me.sayandas.video.VideoResolution;
import me.sayandas.video.VideoResolutionProbeResult;
import me.sayandas.video.VideoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

public class TaskQueueingHandler implements RequestHandler<S3Event, Boolean> {

    private final MediaVideoRepository mediaVideoRepository = MediaVideoRepository.getInstance();
    private final MessageRepository messageRepository = MessageRepository.getInstance();
    private final ObjectMapper om = new ObjectMapper();
    private final Logger log = LogManager.getLogger(TaskQueueingHandler.class);

    @Override
    public Boolean handleRequest(S3Event s3Event, Context context) {
        String queueName = System.getenv(LambdaEnvVariables.TRANSCODE_QUEUE),
                dbUrl = System.getenv(LambdaEnvVariables.DB_URL),
                dbUserName = System.getenv(LambdaEnvVariables.DB_USERNAME),
                dbPassword = System.getenv(LambdaEnvVariables.DB_PASSWORD);

        List.of(queueName, dbUrl, dbPassword, dbUserName).forEach(
                val -> {
                    if(Objects.isNull(val) || val.isBlank()){
                        String errMsg = "Environment variable " + val + " is not configured";
                        log.error(errMsg);
                        throw new IllegalArgumentException(errMsg);
                    }
                }
        );

        long preDBConnTime = System.currentTimeMillis();
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)){
            long postDBConnTime = System.currentTimeMillis();
            log.info("Established Database connection. Time taken to establish DB connection (in ms): {}", (postDBConnTime - preDBConnTime));
            // Configure repository objects
            mediaVideoRepository.setConnection(connection);
            messageRepository.setConnection(connection);

            for(S3EventNotification.S3EventNotificationRecord record: s3Event.getRecords()){
                S3EventNotification.S3BucketEntity bucket = record.getS3().getBucket();
                S3EventNotification.S3ObjectEntity entity = record.getS3().getObject();
                String bucketName = bucket.getName();
                String s3ObjectKey = entity.getUrlDecodedKey();
                log.info("Processing S3 Record: Object Key: {}. Bucket: {}", s3ObjectKey, bucketName);
                // Download S3 File
                String downloadedS3VideoFile = S3Utils.downloadFile(s3ObjectKey, bucketName).getAbsolutePath();
                log.debug("File downloaded from S3 to below location: {}", downloadedS3VideoFile);

                VideoResolutionProbeResult resolutionProbeResult = VideoUtils.getVideoResolution(downloadedS3VideoFile);
                log.info("FFProbe completed. Resolution of input video: {}", resolutionProbeResult);
                List<VideoResolution> lowerResolutions = VideoResolution.getTargetResolutions(VideoResolution.from(
                        resolutionProbeResult.width(),
                        resolutionProbeResult.height()
                ));
                Map<VideoResolution, String> transcodedVersions = new HashMap<>();
                lowerResolutions.forEach(res -> transcodedVersions.put(res, ""));
                log.debug("List of target resolutions: {}", lowerResolutions);

                // Insert row for media video table
                log.debug("START: Inserting video metadata in DB");
                String mediaId = UUID.randomUUID().toString();
                mediaVideoRepository.insert(MediaVideo.builder()
                        .mediaId(mediaId)
                        .transcodedVersions(transcodedVersions)
                        .build());
                log.debug("END: Inserted video metadata to DB");

                // Publish messages to SQS
                List<Message> messages = new ArrayList<>();
                for(VideoResolution targetRes: lowerResolutions){
                    String strJsonMessageBody = om.writeValueAsString( new QueueMessageBody(s3ObjectKey, bucketName, targetRes));
                    log.info("Enqueueing JSON Message Body: {} to queue: {}", strJsonMessageBody, queueName);
                    String messageId = SQSUtil.enqueueMessage(queueName, strJsonMessageBody);
                    log.info("Successfully enqueued message. Message Id = {}", messageId);
                    messages.add(Message.builder()
                            .messageId(messageId)
                            .mediaId(mediaId)
                            .state(Message.MessageState.CREATED)
                            .build());
                }

                log.debug("START: Inserting message metadata DB");
                // Insert row(s) for message table
                messageRepository.insertBulk(messages);
                log.info("END: Inserted message metadata in DB");
            }

        } catch(Exception e){
            log.error("Error occurred when processing S3 Event: {}",LogUtils.getFullErrorMessage(e));
            return false;
        }
        return true;
    }
}
