package com.s3.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.s3.BuildConfig;
import com.s3.R;
import com.s3.model.S3BucketData;
import com.s3.model.S3Credentials;

import java.io.File;

/**
 * Created by amit on 16/8/17.
 */

public class S3Uploader {
    private S3Uploader() {

    }

    private Context context;

    public S3Uploader(@NonNull Context context, @NonNull S3BucketData s3BucketData) {
        this.context = context;
        handleUpload(s3BucketData);
    }


    /*
        * This method performs the upload in three simple steps:
        * 1) Get the transfer manager which allows us to perform uploads to a bucket
        * 2) Create the request object
        * 3) Perform the request and wait for it to complete
        * */
    private void handleUpload(final S3BucketData s3BucketData) {
        TransferUtility transferUtility = setUpAmazonClient(s3BucketData);
        PutObjectRequest po = buildPor(s3BucketData);
        if (s3BucketData.getDialog() != null
                && !s3BucketData.getDialog().isShowing()) {
            s3BucketData.getDialog().show();
        }
        final TransferObserver observer = transferUtility.upload(
                po.getBucketName(),
                s3BucketData.getKey().getName(),
                po.getFile(), po.getMetadata(), po.getCannedAcl()
        );
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
//        It's only when the s3Callback provided isn't null that we will send the broadcast message
                if (s3BucketData.getDialog() != null
                        && s3BucketData.getDialog().isShowing()) {
                    s3BucketData.getDialog().dismiss();
                }
                if (s3BucketData.getS3Callback() == null) {
                    return;
                }
                switch (state) {
                    case COMPLETED:
                        s3BucketData.getS3Callback().onResult(true, state.toString(), s3BucketData);
                        break;
                    case CANCELED:
                    case FAILED:
                        s3BucketData.getS3Callback().onResult(false, state.toString(), s3BucketData);
                        break;
                    default:
                        s3BucketData.getS3Callback().onResult(false, state.toString(), s3BucketData);
                        break;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (s3BucketData.getS3Callback() == null) {
                    return;
                }
                s3BucketData.getS3Callback().onProgressChanged(id, bytesCurrent, bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                if (s3BucketData.getDialog() != null
                        && s3BucketData.getDialog().isShowing()) {
                    s3BucketData.getDialog().dismiss();
                }
                if (s3BucketData.getS3Callback() == null) {
                    return;
                }
                s3BucketData.getS3Callback().onResult(false, ex.getMessage(), s3BucketData);
            }
        });
    }
//endregion

    //    region amazon client setup
    private TransferUtility setUpAmazonClient(S3BucketData s3BucketData) {

        S3Credentials s3Credentials = s3BucketData.getS3Credentials();
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3Credentials.getAccessKey(),
                s3Credentials.getSecretKey());
        AmazonS3Client s3 = new AmazonS3Client(credentials);
        return new TransferUtility(s3, this.context);
    }
//    endregion

    //    region PutObjectRequest creation
    private PutObjectRequest buildPor(S3BucketData s3BucketData
    ) {

        final String bucket = s3BucketData.getBucket();
        final File file = s3BucketData.getKey();
        final boolean deleteFileAfter = s3BucketData.isDeleteAfterUse();

        final PutObjectRequest por = new PutObjectRequest(bucket, file.getName(), file);
        por.setGeneralProgressListener(new ProgressListener() {
            final String url = String.format(context.getString(R.string.s3_format_url), bucket, file.getPath());
            private long uploadStartTime;

            @Override
            public void progressChanged(com.amazonaws.event.ProgressEvent progressEvent) {
                try {
                    if (progressEvent.getEventCode() == ProgressEvent.STARTED_EVENT_CODE) {
                        uploadStartTime = System.currentTimeMillis();
                    } else if (progressEvent.getEventCode() == com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE) {
                        long uploadDurationMillis = System.currentTimeMillis() - uploadStartTime;
                        int bytesPerSecond = (int) (file.length() / (uploadDurationMillis / 1000.0));
                        if (BuildConfig.DEBUG) {
                            double fileSize = file.length() / 1000.0;
                            double uploadDuration = uploadDurationMillis;
                            double uploadSpeed = bytesPerSecond / 1000.0;
                            Log.i(getClass().getSimpleName(), String.format(context.getString(R.string.s3_format_uploaded), fileSize, uploadDuration, uploadSpeed));
                        }

                        if (deleteFileAfter) {
                            file.delete();
                        }
                    } else if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        if (BuildConfig.DEBUG) {
                            Log.e(getClass().getSimpleName(), String.format(context.getString(R.string.s3_format_upload_failed), url));
                        }
                    }
                } catch (Exception excp) {
                    if (BuildConfig.DEBUG)
                        Log.e(getClass().getSimpleName(), "ProgressListener error");
                    excp.printStackTrace();
                }
            }
        });
        por.setCannedAcl(CannedAccessControlList.PublicRead);
        return por;
    }
}
