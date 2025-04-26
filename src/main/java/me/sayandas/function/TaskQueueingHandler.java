package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;

public class TaskQueueingHandler implements RequestHandler<S3Event, Boolean> {

    @Override
    public Boolean handleRequest(S3Event s3Event, Context context) {

        return null;
    }
}
