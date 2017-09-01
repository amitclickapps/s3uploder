package com.s3.model;

public class S3Credentials {

    private static String accessKey;
    private static String secretKey;

    public S3Credentials(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public static String getAccessKey() {
        return accessKey;
    }

    public static String getSecretKey() {
        return secretKey;
    }

}