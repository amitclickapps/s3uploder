package com.s3.callback;

import android.os.Bundle;

import java.io.Serializable;

/*
* actionCallback is intended to be an intent's action, intent which will be then sent in a
* broadcast message.
*
* extra could be anything (could be null too)
* */
public class S3BroadCast implements Serializable {

    private String actionCallback;
    private int extra;
    private S3Callback callback;

    public S3BroadCast(String actionCallback, int extra, S3Callback callback) {
        this.actionCallback = actionCallback;
        this.extra = extra;
        this.callback = callback;
    }

    public String getActionCallback() {
        return actionCallback;
    }

    public int getExtra() {
        return extra;
    }

    public S3Callback getCallback() {
        return callback;
    }

    public interface S3Callback {
        void onProgressChanged(int extra, long bytesCurrent, long bytesTotal);
    }
}