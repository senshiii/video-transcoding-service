package me.sayandas.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.IOException;
import java.util.logging.Logger;

public class S3Utils {

    private static S3Client s3Client;
    private static Logger logger = LogUtils.getLoggerWithConsoleHandler(S3Utils.class.getName());

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

}
