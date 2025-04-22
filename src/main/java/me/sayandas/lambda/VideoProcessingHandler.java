package me.sayandas.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import java.util.List;

public class VideoProcessingHandler implements RequestHandler<SQSEvent, Boolean> {

    @Override
    public Boolean handleRequest(SQSEvent sqsEvent, Context context) {

        SQSMessage message = sqsEvent.getRecords().get(0);

        String messageBody = message.getBody();

        // Insert row in DB for current Message ID
        // Extract S3 Location of image & quality data from SQS data
        // Read & load image
        // Run Ffmpeg - Generate
        // Update row in DB for current Message ID

        return true;
    }
}
