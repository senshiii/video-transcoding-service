package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.db.model.Message;
import me.sayandas.db.repository.MediaVideoRepository;
import me.sayandas.db.repository.MessageRepository;
import me.sayandas.utils.LogUtils;
import me.sayandas.utils.S3Utils;
import me.sayandas.utils.SQSUtil;
import me.sayandas.video.VideoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Objects;

public class VideoProcessingHandler implements RequestHandler<SQSEvent, Boolean> {

    final Logger logger = LogManager.getLogger(VideoProcessingHandler.class);
    final ObjectMapper objectMapper = new ObjectMapper();
    final MessageRepository messageRepository = MessageRepository.getInstance();
    final MediaVideoRepository mediaVideoRepository = MediaVideoRepository.getInstance();

    @Override
    public Boolean handleRequest(SQSEvent sqsEvent, Context context) {
        // Extract SQS message details
        SQSMessage message = sqsEvent.getRecords().get(0);
        logger.info("Processing SQS Message: {}. Receipt Handle: {}", message.getMessageId(), message.getReceiptHandle());
        String queueName = System.getenv(LambdaEnvVariables.TRANSCODE_QUEUE),
                destinationBucket = System.getenv(LambdaEnvVariables.DESTINATION_BUCKET),
                dbUrl = System.getenv(LambdaEnvVariables.DB_URL),
                dbUserName = System.getenv(LambdaEnvVariables.DB_USERNAME),
                dbPassword = System.getenv(LambdaEnvVariables.DB_PASSWORD);
        List.of(queueName, dbUrl, dbPassword, dbUserName).forEach(
                val -> {
                    if(Objects.isNull(val) || val.isBlank())
                        throw new IllegalArgumentException("Environment variable " + val + " is not configured");
                }
        );
        long preDBConnTime = System.currentTimeMillis();
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)){
            long postDBConnTime = System.currentTimeMillis();
            logger.info("Established Database connection. Time taken to establish DB connection (in ms): {}", postDBConnTime - preDBConnTime);
            mediaVideoRepository.setConnection(connection);
            messageRepository.setConnection(connection);
           QueueMessageBody messageBody = objectMapper.readValue(message.getBody(), QueueMessageBody.class);

            // Fetch DB row from message table
            Message dbMsgRow = messageRepository.fetchById(message.getMessageId());

            // If message is received again, then delete message from queue
            if(dbMsgRow.getReceiptHandle() != null && dbMsgRow.getReceiptHandle().isBlank()){
                logger.info("Message already processed before. Processed receipt handle: {}. Current Receipt Handle: {}", dbMsgRow.getReceiptHandle(), message.getReceiptHandle());
                SQSUtil.deleteMessage(queueName, dbMsgRow.getReceiptHandle());
                return true;
            }

            // Download file from S3
           String filePath = S3Utils.downloadFile(messageBody.s3ObjectKey(), messageBody.s3BucketName()).getAbsolutePath();
           String outputFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "_output" + filePath.substring(filePath.lastIndexOf("."));
           logger.debug("Output file path: {}", outputFilePath);
           Path p = Paths.get(outputFilePath);
           VideoUtils.generateVideo(Paths.get(filePath), p, messageBody.targetResolution());
           logger.info("Generated output video. Location = {}", outputFilePath);

           // Upload file to S3
           String objectKey = dbMsgRow.getMediaId() + "/" + dbMsgRow.getMediaId() + "_" + messageBody.targetResolution() + ".mp4";
           S3Utils.uploadFile(objectKey, destinationBucket, p);
           String url = S3Utils.getObjectUrl(objectKey, destinationBucket);

           // Update Media Video table
           mediaVideoRepository.updateTranscodedVersionsJsonById(dbMsgRow.getMediaId(), messageBody.targetResolution(), url);

           // Update message table
           messageRepository.updateById(message.getMessageId(), Message
                   .builder()
                           .state(Message.MessageState.PROCESSED)
                           .receiptHandle(message.getReceiptHandle())
                   .build());

           // Delete SQS Message
           SQSUtil.deleteMessage(queueName, message.getReceiptHandle());
        } catch(Exception e){
            String errorMessage = LogUtils.getFullErrorMessage(e);
            logger.error("Error occurred when processing SQS Message: {}", errorMessage);
            try {
                messageRepository.updateById(message.getMessageId(), Message
                        .builder()
                        .state(Message.MessageState.PROCESSED)
                        .receiptHandle(message.getReceiptHandle())
                        .build());
            } catch (Exception ex) {
                logger.error("Error occurred when updating message table: {}", LogUtils.getFullErrorMessage(e));
            }
            return false;
        }
        return true;
    }

}
