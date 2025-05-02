package me.sayandas.utils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

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

    public static void enqueueMessage(String queueName, String messageBody){
        SQSUtil.enqueueMessage(queueName, messageBody, Collections.emptyMap());
    }

    public static void enqueueMessage(String queueName,
                                      String messageBody, Map<String, String> messageAttributes){
        String queueURL;
        try {
            GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();
            GetQueueUrlResponse queueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);
            queueURL = queueUrlResponse.queueUrl();
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .messageBody(messageBody)
                    .queueUrl(queueURL)
                    .build();
            sqsClient.sendMessage(sendMsgRequest);
        }catch(Exception e){
            throw new RuntimeException("Error occurred when publishing message to queue", e);
        }
    }

}
