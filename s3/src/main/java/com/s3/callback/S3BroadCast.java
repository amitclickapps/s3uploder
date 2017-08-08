package com.s3.callback;

import java.io.Serializable;

/*
* actionCallback is intended to be an intent's action, intent which will be then sent in a
* broadcast message.
*
* extra could be anything (could be null too)
* */
public class S3BroadCast implements Serializable {

    private String actionCallback;
    private Serializable extra;
    private S3Callback callback;

    public S3BroadCast(String actionCallback, Serializable extra, S3Callback callback) {
        this.actionCallback = actionCallback;
        this.extra = extra;
        this.callback = callback;
    }

    public String getActionCallback() {
        return actionCallback;
    }

    public Serializable getExtra() {
        return extra;
    }

    public S3Callback getCallback() {
        return callback;
    }

    public interface S3Callback {
        void onProgressChanged(Serializable extra, long bytesCurrent, long bytesTotal);
    }
}