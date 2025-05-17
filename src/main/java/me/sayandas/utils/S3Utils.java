package me.sayandas.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;


public class S3Utils {

    private static S3Client s3Client;
    private static Logger log = LogUtils.getLoggerWithConsoleHandler(S3Utils.class.getName());

    static{
        s3Client = S3Client.builder().region(Region.US_EAST_1).build();
    }

    public static byte[] readObjectAsBytes(String bucketName, String objectKey) throws IOException {
        ResponseInputStream<GetObjectResponse> res = s3Client.getObject(GetObjectRequest.builder().key(objectKey).bucket(bucketName).build());
        return res.readAllBytes();
    }

    public static String getObjectUrl(String objectKey, String bucketName){
        GetUrlRequest getUrlRequest = GetUrlRequest.builder().key(objectKey).bucket(bucketName).build();
        return s3Client.utilities().getUrl(getUrlRequest).toExternalForm();
    }

    public static File downloadFile(String s3ObjectKey, String s3BucketName) throws IOException {
        byte[] videoData = S3Utils.readObjectAsBytes(s3BucketName, s3ObjectKey);
        String tempFileName = s3BucketName + "_" + s3ObjectKey + UUID.randomUUID();
        File downloadedVideoFile = File.createTempFile(tempFileName, ".mp4");
        try(OutputStream os = new FileOutputStream(downloadedVideoFile)){
            os.write(videoData);
        }catch(IOException e){
            log.severe(LogUtils.getFullErrorMessage("Error occurred when downloading file to temp location", e));
            throw e;
        }
        return downloadedVideoFile;
    }

    public static void uploadFile(String s3ObjectKey, String bucketName, Path filePath){
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .key(s3ObjectKey)
                .bucket(bucketName)
                .build();
        s3Client.putObject(putObjectRequest, filePath);
    }

}
