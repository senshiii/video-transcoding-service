package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.model.queue.QueueMessageBody;

public class VideoProcessingHandler implements RequestHandler<SQSEvent, Boolean> {

    @Override
    public Boolean handleRequest(SQSEvent sqsEvent, Context context) {

        SQSMessage message = sqsEvent.getRecords().get(0);
        QueueMessageBody messageBody;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
           messageBody = objectMapper.readValue(message.getBody(), QueueMessageBody.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Create temp video file

        // Insert row in DB for current Message ID

        // Extract S3 Location of image & quality data from SQS data
        // Read & load image
        // Run Ffmpeg - Generate
        // Update row in DB for current Message ID

        return true;
    }
}
