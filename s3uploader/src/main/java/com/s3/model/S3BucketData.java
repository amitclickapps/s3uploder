package com.s3.model;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.s3.callback.S3Callback;
import com.s3.service.S3Uploader;

import java.io.File;
import java.io.Serializable;

public class S3BucketData {

    private String region;
    private String bucket, bucketFolder, fileName;
    private File key;
    private boolean deleteAfterUse = false;
    private S3Callback s3Callback;
    private Dialog dialog;
    private Context context;

    private S3BucketData() {
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

    public Context getContext() {
        return context;
    }

    public String getBucketFolder() {
        return bucketFolder;
    }

    public String getFileName() {
        return fileName;
    }

    public static class Builder implements Serializable {

        private S3BucketData s3BucketData;

        public Builder(@NonNull Context context) {
            s3BucketData = new S3BucketData();
            s3BucketData.context = context;
        }

        public Builder setRegion(@NonNull String region) {
            s3BucketData.region = region;
            return this;
        }

        public Builder setBucket(@NonNull String bucket) {
            s3BucketData.bucket = bucket;
            return this;
        }

        public Builder setBucketFolder(@NonNull String bucketFolder) {
            s3BucketData.bucketFolder = bucketFolder;
            return this;
        }

        public Builder setKey(@NonNull File key, @NonNull String fileName) {
            s3BucketData.key = key;
            s3BucketData.fileName = fileName;
            return this;
        }


        public Builder setDeleteFileAfterUse(boolean deleteFileAfterUse) {
            s3BucketData.deleteAfterUse = deleteFileAfterUse;
            return this;
        }

        public Builder setS3Callback(@NonNull S3Callback callback) {
            s3BucketData.s3Callback = callback;
            return this;
        }

        public Builder progressDialog(Dialog dialog) {
            s3BucketData.dialog = dialog;
            return this;
        }

        public S3BucketData build() {
            new S3Uploader(s3BucketData);
            return s3BucketData;
        }
    }
}