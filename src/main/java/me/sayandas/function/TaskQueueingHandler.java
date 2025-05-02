package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.model.queue.QueueMessageBody;
import me.sayandas.utils.S3Utils;
import me.sayandas.utils.SQSUtil;
import me.sayandas.video.VideoResolution;
import me.sayandas.video.VideoResolutionProbeResult;
import me.sayandas.video.VideoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

public class TaskQueueingHandler implements RequestHandler<S3Event, Boolean> {

    private final ObjectMapper om = new ObjectMapper();
    private final Logger log = Logger.getLogger(this.getClass().getName());

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

        try {
            byte[] videoData = S3Utils.readObjectAsBytes(bucket.getName(), entity.getKey());
            String tempFileName = bucket.getName() + "_" + entity.getKey();
            File downloadedVideoFile = File.createTempFile(tempFileName, ".mp4");
            OutputStream os = new FileOutputStream(downloadedVideoFile);
            os.write(videoData);
            VideoResolutionProbeResult resolutionProbeResult = VideoUtils.getVideoResolution(downloadedVideoFile.getAbsolutePath());
            List<VideoResolution> lowerResolutions = VideoResolution.fetchAllResolutionsBelow(VideoResolution.from(
                    resolutionProbeResult.width(),
                    resolutionProbeResult.height()
            ));
            for(VideoResolution vidRes: lowerResolutions){
                String queueName = System.getenv("transcoder-queue");
                log.finest("Publishing to queue: " + queueName);
                QueueMessageBody messageBody = new QueueMessageBody(entity.getKey(), bucket.getName(), vidRes);
                String strJsonMessageBody = om.writeValueAsString(messageBody);
                log.finer("JSON Message Body: " + strJsonMessageBody);
                SQSUtil.enqueueMessage(queueName, strJsonMessageBody);
            }
            os.close();
        } catch(Exception e){
            log.severe("Exception occurred when processing message: " + e.getMessage());
        }
        return null;
    }
}
