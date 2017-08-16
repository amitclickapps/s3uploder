package com.s3.model;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by clickapps on 8/8/17.
 */

public class S3Response implements Serializable{

    private boolean status;
    private int extra;
    private String message;


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
