package me.sayandas.utils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collections;

import java.util.Map;

public class SQSUtil {

    private static SqsClient sqsClient;

    static {
        try {
            sqsClient = SqsClient.builder().region(Region.US_EAST_1).build();
        }catch(Exception e){
            throw new RuntimeException("Failed to initialize Amazon SQS Client", e);
        }
    }

    public static String enqueueMessage(String queueName, String messageBody) {
        return SQSUtil.enqueueMessage(queueName, messageBody, Collections.emptyMap());
    }

    public static String enqueueMessage(String queueName,
                                      String messageBody, Map<String, String> messageAttributes){
        String queueURL;
        try {
            queueURL = getQueueUrl(queueName).queueUrl();
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .messageBody(messageBody)
                    .queueUrl(queueURL)
                    .build();
            SendMessageResponse res = sqsClient.sendMessage(sendMsgRequest);
            return res.messageId();
        }catch(Exception e){
            throw new RuntimeException("Error occurred when publishing message to queue", e);
        }
    }

    public static GetQueueUrlResponse getQueueUrl(String queueName){
        GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        return sqsClient.getQueueUrl(getQueueUrlRequest);

    }

    public static boolean deleteMessage(String queueName, String receiptHandle) {
        GetQueueUrlRequest queueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
        String queueUrl = getQueueUrl(queueName).queueUrl();
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
        return true;
    }

}
