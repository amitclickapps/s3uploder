package com.s3.model;

import android.app.Dialog;

import com.s3.callback.S3Callback;

import java.io.File;
import java.io.Serializable;

public class S3BucketData {

    private S3Credentials s3Credentials;
    private String region;
    private String bucket;
    private File key;
    private boolean deleteAfterUse = false;
    private S3Callback s3Callback;
    private Dialog dialog;

    private S3BucketData() {
    }

    public S3Credentials getS3Credentials() {
        return s3Credentials;
    }

    public String getRegion() {
        return region;
    }

    public String getBucket() {
        return bucket;
    }

    public File getKey() {
        return key;
    }

    public S3Callback getS3Callback() {
        return s3Callback;
    }

    public boolean isDeleteAfterUse() {
        return deleteAfterUse;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public static class Builder implements Serializable {

        private S3BucketData s3BucketData;

        public Builder() {
            s3BucketData = new S3BucketData();
        }

        public Builder setCredentials(S3Credentials s3Credentials) {
            s3BucketData.s3Credentials = s3Credentials;
            return this;
        }

        public Builder setRegion(String region) {
            s3BucketData.region = region;
            return this;
        }

        public Builder setBucket(String bucket) {
            s3BucketData.bucket = bucket;
            return this;
        }

        public Builder setKey(File key) {
            s3BucketData.key = key;
            return this;
        }


        public Builder setDeleteFileAfterUse(boolean deleteFileAfterUse) {
            s3BucketData.deleteAfterUse = deleteFileAfterUse;
            return this;
        }

        public Builder setS3Callback(S3Callback callback) {
            s3BucketData.s3Callback = callback;
            return this;
        }

        public Builder progressDialog(Dialog dialog) {
            s3BucketData.dialog = dialog;
            return this;
        }

        public S3BucketData build() {
            return s3BucketData;
        }
    }
}