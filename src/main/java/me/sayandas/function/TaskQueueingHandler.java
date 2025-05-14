package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.db.model.MediaVideo;
import me.sayandas.db.model.Message;
import me.sayandas.db.service.MediaVideoRepository;
import me.sayandas.db.service.MessageRepository;
import me.sayandas.model.queue.QueueMessageBody;
import me.sayandas.utils.LogUtils;
import me.sayandas.utils.S3Utils;
import me.sayandas.utils.SQSUtil;
import me.sayandas.video.VideoResolution;
import me.sayandas.video.VideoResolutionProbeResult;
import me.sayandas.video.VideoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.logging.Logger;

public class TaskQueueingHandler implements RequestHandler<S3Event, Boolean> {

    private final String ENV_VAR_TRANSCODER_QUEUE = "transcode_queue";
    private final String ENV_VAR_DB_JDBC_URL = "db_jdbc_url";
    private final String ENV_VAR_DB_JDBC_USERNAME = "db_jdbc_username";
    private final String ENV_VAR_DB_JDBC_PASSWORD = "db_jdbc_password";

    private final ObjectMapper om = new ObjectMapper();
    private final Logger log = LogUtils.getLoggerWithConsoleHandler(this.getClass().getName());

    @Override
    public Boolean handleRequest(S3Event s3Event, Context context) {
        log.finest("Records: " + s3Event.getRecords());

        String queueName = System.getenv(ENV_VAR_TRANSCODER_QUEUE),
                dbUrl = System.getenv(ENV_VAR_DB_JDBC_URL),
                dbUserName = System.getenv(ENV_VAR_DB_JDBC_USERNAME),
                dbPassword = System.getenv(ENV_VAR_DB_JDBC_PASSWORD);
        List.of(queueName, dbUrl, dbPassword, dbUserName).forEach(
                val -> {
                    if(Objects.isNull(val) || val.isBlank())
                        throw new IllegalArgumentException("Environment variable " + val + " is not configured");
                }
        );

        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)){
            log.fine("Established Database connection");
            // Extract data from S3Event
            S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);
            S3EventNotification.S3BucketEntity bucket = record.getS3().getBucket();
            S3EventNotification.S3ObjectEntity entity = record.getS3().getObject();
            String bucketName = bucket.getName();
            String s3ObjectKey = entity.getUrlDecodedKey();
            log.finest("Extracted data from S3 event: Object Key: " + s3ObjectKey + ". Bucket: " + bucketName);

            // Acquire repository objects
            MediaVideoRepository mediaVideRepo = MediaVideoRepository.getInstance();
            mediaVideRepo.setConnection(connection);

            MessageRepository messageRepo = MessageRepository.getInstance();
            messageRepo.setConnection(connection);

            String downloadedS3VideoFile = this.downloadS3File(s3ObjectKey, bucketName);
            log.finest("Downloaded file from S3 to file location: " + downloadedS3VideoFile);

            VideoResolutionProbeResult resolutionProbeResult = VideoUtils.getVideoResolution(downloadedS3VideoFile);
            log.finest("FFProbe completed. Resolution of input video: " + resolutionProbeResult);

            List<VideoResolution> lowerResolutions = VideoResolution.fetchAllResolutionsBelow(VideoResolution.from(
                    resolutionProbeResult.width(),
                    resolutionProbeResult.height()
            ));
            Map<VideoResolution, String> transcodedVersions = new HashMap<>();
            lowerResolutions.forEach(res -> transcodedVersions.put(res, ""));

            log.finest("List of target resolutions: " + lowerResolutions);

            // Insert row for media video table
            log.finer("Inserting record for media in DB");
            String mediaId = UUID.randomUUID().toString();
            mediaVideRepo.insert(MediaVideo.builder()
                    .mediaId(mediaId)
                    .url(S3Utils.getObjectUrl(s3ObjectKey, bucketName))
                    .transcodedVersions(transcodedVersions)
                    .build());
            log.finer("Record for media inserted successfully in DB");

            // Publish messages to SQS
            List<Message> messages = new ArrayList<>();

            for(VideoResolution targetRes: lowerResolutions){
                QueueMessageBody messageBody = new QueueMessageBody(s3ObjectKey, bucketName, targetRes);
                String strJsonMessageBody = om.writeValueAsString(messageBody);
                log.finer("Enqueueing JSON Message Body: " + strJsonMessageBody + " to queue: " + queueName);
                String messageId = SQSUtil.enqueueMessage(queueName, strJsonMessageBody);
                log.finer("Successfully enqueued message. Message Id = " + messageId);
                messages.add(Message.builder()
                        .messageId(messageId)
                        .mediaId(mediaId).build());
            }

            log.finer("Inserting record for messages in DB");
            // Insert row(s) for message table
            messageRepo.insertBulk(messages);
            log.finer("Successfully inserted record for messages in DB");

        } catch(Exception e){
            log.severe("Exception occurred when processing message: " + e.getMessage());
        }
        return null;
    }

    public String downloadS3File(String s3ObjectKey, String s3BucketName) throws IOException {
        log.entering(this.getClass().getName(), "downloadS3File");
        byte[] videoData = S3Utils.readObjectAsBytes(s3BucketName, s3ObjectKey);
        String tempFileName = s3BucketName + "_" + s3ObjectKey;
        File downloadedVideoFile = File.createTempFile(tempFileName, ".mp4");
        OutputStream os = new FileOutputStream(downloadedVideoFile);
        os.write(videoData);
        os.close();
        log.exiting(this.getClass().getName(), "downloadS3File");
        return downloadedVideoFile.getAbsolutePath();
    }
}
