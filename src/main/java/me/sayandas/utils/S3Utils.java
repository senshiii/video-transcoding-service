package me.sayandas.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;

public class S3Utils {

    private static S3Client s3Client;

    static{
        s3Client = S3Client.builder().region(Region.US_EAST_1).build();
    }

    public static byte[] readObjectAsBytes(String bucketName, String objectKey) throws IOException {
        ResponseInputStream<GetObjectResponse> res = s3Client.getObject(GetObjectRequest.builder().key(objectKey).bucket(bucketName).build());
        return res.readAllBytes();
    }

}
