package com.s3.model;

import java.io.Serializable;

public class S3Credentials implements Serializable {

    private String accessKey;
    private String secretKey;

    public S3Credentials(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

}