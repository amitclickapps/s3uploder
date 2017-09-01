package com.s3.callback;

import android.os.Bundle;

import com.s3.model.S3BucketData;

import java.io.Serializable;

/*
* actionCallback is intended to be an intent's action, intent which will be then sent in a
* broadcast message.
*
* extra could be anything (could be null too)
* */
public interface S3Callback {

    void onProgressChanged(int extra, long bytesCurrent, long bytesTotal);

    void onResult(boolean status, String uploadedUrl, S3BucketData s3BucketData);
}