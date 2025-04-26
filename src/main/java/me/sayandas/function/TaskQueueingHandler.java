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
        S3EventNotification.S3BucketEntity bucket = record.getS3().getBucket();
        System.out.println("bucket.getName() = " + bucket.getName());
        System.out.println("bucket.getArn() = " + bucket.getArn());
        System.out.println("bucket.getOwnerIdentity() = " + bucket.getOwnerIdentity());
        S3EventNotification.S3ObjectEntity entity = record.getS3().getObject();
        System.out.println("entity.getUrlDecodedKey() = " + entity.getUrlDecodedKey());
        System.out.println("entity.getSizeAsLong() = " + entity.getSizeAsLong());
        System.out.println("entity.geteTag() = " + entity.geteTag());
        System.out.println("entity.getKey() = " + entity.getKey());
        System.out.println("entity.getVersionId() = " + entity.getVersionId());
        System.out.println("entity.getSizeAsLong() = " + entity.getSizeAsLong());
        System.out.println("record = " + record);
        return null;
    }
}
