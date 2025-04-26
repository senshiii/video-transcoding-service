package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;

public class TaskQueueingHandler implements RequestHandler<S3Event, Boolean> {

    @Override
    public Boolean handleRequest(S3Event s3Event, Context context) {
        System.out.println("Records: " + s3Event.getRecords());
        S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);
        System.out.println("record = " + record);
        return null;
    }
}
