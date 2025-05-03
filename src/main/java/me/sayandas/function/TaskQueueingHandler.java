package me.sayandas.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.model.queue.QueueMessageBody;
import me.sayandas.utils.LogUtils;
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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskQueueingHandler implements RequestHandler<S3Event, Boolean> {

    private final ObjectMapper om = new ObjectMapper();
    private final Logger log = LogUtils.getLoggerWithConsoleHandler(this.getClass().getName());

    @Override
    public Boolean handleRequest(S3Event s3Event, Context context) {
        log.finest("Records: " + s3Event.getRecords());
        S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);
        S3EventNotification.S3BucketEntity bucket = record.getS3().getBucket();
        log.finest("bucket.getName() = " + bucket.getName());
        log.finest("bucket.getArn() = " + bucket.getArn());
        log.finest("bucket.getOwnerIdentity() = " + bucket.getOwnerIdentity());
        S3EventNotification.S3ObjectEntity entity = record.getS3().getObject();
        log.finest("entity.getUrlDecodedKey() = " + entity.getUrlDecodedKey());
        log.finest("entity.getSizeAsLong() = " + entity.getSizeAsLong());
        log.finest("entity.geteTag() = " + entity.geteTag());
        log.finest("entity.getKey() = " + entity.getKey());
        log.finest("entity.getVersionId() = " + entity.getVersionId());
        log.finest("entity.getSizeAsLong() = " + entity.getSizeAsLong());
        log.finest("record = " + record);

        String queueName = System.getenv("transcoder-queue");

        try {
            log.finest("Reading file as bytes form S3 | Start");
            byte[] videoData = S3Utils.readObjectAsBytes(bucket.getName(), entity.getUrlDecodedKey());
            String tempFileName = bucket.getName() + "_" + entity.getKey();
            File downloadedVideoFile = File.createTempFile(tempFileName, ".mp4");
            OutputStream os = new FileOutputStream(downloadedVideoFile);
            os.write(videoData);
            log.finest("Reading file as bytes form S3 | Complete");
            VideoResolutionProbeResult resolutionProbeResult = VideoUtils.getVideoResolution(downloadedVideoFile.getAbsolutePath());
            List<VideoResolution> lowerResolutions = VideoResolution.fetchAllResolutionsBelow(VideoResolution.from(
                    resolutionProbeResult.width(),
                    resolutionProbeResult.height()
            ));
            log.finest("List of target resolutions: " + lowerResolutions);
            log.finest("Publishing to queue: " + queueName);
            for(VideoResolution vidRes: lowerResolutions){
                QueueMessageBody messageBody = new QueueMessageBody(entity.getKey(), bucket.getName(), vidRes);
                String strJsonMessageBody = om.writeValueAsString(messageBody);
                log.finer("Posting JSON Message Body: " + strJsonMessageBody + " to queue: " + queueName);
                SQSUtil.enqueueMessage(queueName, strJsonMessageBody);
            }
            os.close();
        } catch(Exception e){
            log.severe("Exception occurred when processing message: " + e.getMessage());
        }
        return null;
    }
}
