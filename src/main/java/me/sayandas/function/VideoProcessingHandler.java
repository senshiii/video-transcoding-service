package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.db.model.Message;
import me.sayandas.db.service.MediaVideoRepository;
import me.sayandas.db.service.MessageRepository;
import me.sayandas.model.queue.QueueMessageBody;
import me.sayandas.utils.LogUtils;
import me.sayandas.utils.S3Utils;
import me.sayandas.utils.SQSUtil;
import me.sayandas.video.VideoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class VideoProcessingHandler implements RequestHandler<SQSEvent, Boolean> {

    final Logger logger = LogUtils.getLoggerWithConsoleHandler(this.getClass().getName());
    final ObjectMapper objectMapper = new ObjectMapper();
    final MessageRepository messageRepository = MessageRepository.getInstance();
    final MediaVideoRepository mediaVideoRepository = MediaVideoRepository.getInstance();


    @Override
    public Boolean handleRequest(SQSEvent sqsEvent, Context context) {
        // Extract SQS message details
        SQSMessage message = sqsEvent.getRecords().get(0);
        logger.fine("Processing SQS Message: " + message.getMessageId() + ". Receipt Handle: " + message.getReceiptHandle());

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

        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)){
            mediaVideoRepository.setConnection(connection);
            messageRepository.setConnection(connection);

           QueueMessageBody messageBody = objectMapper.readValue(message.getBody(), QueueMessageBody.class);

           // Fetch DB row from message table
            Message dbMsgRow = messageRepository.fetchById(message.getMessageId());
            // If message is received again, then delete message from queue
            if(dbMsgRow.getReceiptHandle() != null && dbMsgRow.getReceiptHandle().isBlank()){
                logger.fine("Message already processed before. Processed receipt handle: " + dbMsgRow.getReceiptHandle() + ". Current Receipt Handle: " + message.getReceiptHandle());
                SQSUtil.deleteMessage(queueName, dbMsgRow.getReceiptHandle());
                return true;
            }

           // Download file from S3
           String filePath = S3Utils.downloadFile(messageBody.s3ObjectKey(), messageBody.s3BucketName()).getAbsolutePath();
           String outputFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "_output" + filePath.substring(filePath.lastIndexOf("."));
           logger.finest("Output file path: " + outputFilePath);
           Path p = Paths.get(outputFilePath);
           VideoUtils.generateVideo(Paths.get(filePath), p, messageBody.targetResolution());
           logger.fine("Generated output video. Location = " + outputFilePath);

           // Upload file to S3
           String objectKey = dbMsgRow.getMediaId() + "/" + dbMsgRow.getMediaId() + "_" + messageBody.targetResolution() + ".mp4";
           S3Utils.uploadFile(objectKey, destinationBucket, p);
           String url = S3Utils.getObjectUrl(objectKey, destinationBucket);

           // Update Media Video table
           mediaVideoRepository.updateTranscodedVersionsJsonById(dbMsgRow.getMediaId(), messageBody.targetResolution(), url);

           // Delete SQS Message
           SQSUtil.deleteMessage(queueName, message.getReceiptHandle());
        } catch(Exception e){
            logger.severe(LogUtils.getFullErrorMessage("Error occurred when processing SQS Message", e));
            return false;
        }
        return true;
    }

}
