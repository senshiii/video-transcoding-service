package me.sayandas.model.queue;

import me.sayandas.video.VideoResolution;

public record QueueMessageBody(String s3ObjectKey, String s3BucketName, VideoResolution targetResolution) {
}
