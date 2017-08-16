package com.s3.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.s3.R;
import com.s3.callback.S3BroadCast;
import com.s3.model.S3BucketData;
import com.s3.model.S3Credentials;
import com.s3.model.S3Response;

import java.io.File;
import java.io.Serializable;

/*
* It's important to understand that this service doesn't know ANYONE. It's decoupled from the
* rest of the app. It receives the minimum necessary information to perform an upload to an
* amazon bucket and IF AND ONLY IF the s3Callback object is not null, it sends a broadcast message
* to whomever it may concern.
* */
public class S3UploadService extends IntentService {

    private static final String TAG = "S3UploadService";

    private static final String ACTION_UPLOAD = "com.s3.service.S3UploadService";

    public static final String EXTRA_SERIALIZABLE = "EXTRA_SERIALIZABLE";

    private static final String EXTRA_S3_BUCKET_DATA = "com.s3.service.live.extra.S3_BUCKET_DATA";
    private static final String EXTRA_FILE = "com.s3.service.live.extra.FILE";
    private static final String EXTRA_DELETE_FILE = "com.s3.service.live.extra.DELETE_FILE";
    private static final String EXTRA_S3_CALLBACK = "com.s3.service.live.extra.S3_CALLBACK";

    private static boolean VERBOSE = true;

    public S3UploadService() {
        super(TAG);
    }

    /*
    * Helper method to start service
    * */
    public static void upload(Context context, S3BucketData s3BucketData, S3BroadCast s3Callback) {
        Intent intent = new Intent(context, S3UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        Bundle bundle = new Bundle();
        //bundle.putSerializable(EXTRA_S3_BUCKET_DATA, s3BucketData);
//        intent.putExtra(EXTRA_S3_BUCKET_DATA, bundle);
//        intent.putExtra(EXTRA_FILE, s3BucketData.getKey());
//        intent.putExtra(EXTRA_DELETE_FILE, s3BucketData.isDeleteAfterUse());
//        intent.putExtra(EXTRA_S3_CALLBACK, s3Callback);
        bundle.putSerializable(EXTRA_FILE, s3BucketData.getKey());
        bundle.putBoolean(EXTRA_DELETE_FILE, s3BucketData.isDeleteAfterUse());
        bundle.putSerializable(EXTRA_S3_CALLBACK, s3Callback);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    public static void upload(Context context, S3BucketData s3BucketData) {
        upload(context, s3BucketData, null);
    }

    //    region onHandleIntent()

    /*
    * We simply retrieve the extras and call handleUpload()
    * */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        final String action = intent.getAction();
        if (!ACTION_UPLOAD.equals(action)) return;

        S3BucketData s3BucketData = (S3BucketData) intent.getSerializableExtra(EXTRA_S3_BUCKET_DATA);
        File file = (File) intent.getSerializableExtra(EXTRA_FILE);
        boolean deleteFileAfter = intent.getBooleanExtra(EXTRA_DELETE_FILE, true);
        S3BroadCast s3Callback = (S3BroadCast) intent.getSerializableExtra(EXTRA_S3_CALLBACK);
        handleUpload(s3BucketData, s3Callback);
    }
//    endregion

    //    region handleUpload

    /*
    * This method performs the upload in three simple steps:
    * 1) Get the transfer manager which allows us to perform uploads to a bucket
    * 2) Create the request object
    * 3) Perform the request and wait for it to complete
    * */
    private void handleUpload(S3BucketData s3BucketData, final S3BroadCast s3Callback) {
        TransferUtility transferUtility = setUpAmazonClient(s3BucketData);
        PutObjectRequest po = buildPor(s3BucketData);

        final TransferObserver observer = transferUtility.upload(
                po.getBucketName(),
                s3BucketData.getKey().getName(),
                po.getFile(), po.getMetadata(), po.getCannedAcl()
        );

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
//        It's only when the s3Callback provided isn't null that we will send the broadcast message
                if (s3Callback == null) return;
                S3Response s3Response = new S3Response();
                s3Response.setExtra(s3Callback.getExtra());
                s3Response.setMessage(state.toString());
                switch (state) {
                    case COMPLETED:
                        s3Response.setStatus(true);
                        Intent intent = new Intent(s3Callback.getActionCallback());
                        intent.putExtra(EXTRA_SERIALIZABLE, s3Response);
                        sendBroadcast(intent);
                        break;
                    case CANCELED:
                        s3Response.setStatus(false);
                        intent = new Intent(s3Callback.getActionCallback());
                        intent.putExtra(EXTRA_SERIALIZABLE, s3Response);
                        sendBroadcast(intent);
                        break;
                    case FAILED:
                        s3Response.setStatus(false);
                        intent = new Intent(s3Callback.getActionCallback());
                        intent.putExtra(EXTRA_SERIALIZABLE, s3Response);
                        sendBroadcast(intent);
                        break;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (s3Callback == null) return;
                s3Callback.getCallback().onProgressChanged(s3Callback.getExtra(), bytesCurrent, bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                if (s3Callback == null) return;
                S3Response s3Response = new S3Response();
                s3Response.setExtra(s3Callback.getExtra());
                s3Response.setMessage(ex.getMessage());
                s3Response.setStatus(false);
                Intent intent = new Intent(s3Callback.getActionCallback());
                intent.putExtra(EXTRA_SERIALIZABLE, s3Response);
                sendBroadcast(intent);
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
        TransferUtility transferUtility = new TransferUtility(s3, this);
        return transferUtility;
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
            final String url = String.format(getString(R.string.s3_format_url), bucket, file.getPath());
            private long uploadStartTime;

            @Override
            public void progressChanged(com.amazonaws.event.ProgressEvent progressEvent) {
                try {
                    if (progressEvent.getEventCode() == ProgressEvent.STARTED_EVENT_CODE) {
                        uploadStartTime = System.currentTimeMillis();
                    } else if (progressEvent.getEventCode() == com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE) {
                        long uploadDurationMillis = System.currentTimeMillis() - uploadStartTime;
                        int bytesPerSecond = (int) (file.length() / (uploadDurationMillis / 1000.0));
                        if (VERBOSE) {
                            double fileSize = file.length() / 1000.0;
                            double uploadDuration = uploadDurationMillis;
                            double uploadSpeed = bytesPerSecond / 1000.0;
                            Log.i(TAG, String.format(S3UploadService.this.getString(R.string.s3_format_uploaded), fileSize, uploadDuration, uploadSpeed));
                        }

                        if (deleteFileAfter) {
                            file.delete();
                        }
                    } else if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        if (VERBOSE)
                            Log.e(TAG, String.format(S3UploadService.this.getString(R.string.s3_format_upload_failed), url));
                    }
                } catch (Exception excp) {
                    if (VERBOSE) Log.e(TAG, "ProgressListener error");
                    excp.printStackTrace();
                }
            }
        });
        por.setCannedAcl(CannedAccessControlList.PublicRead);
        return por;
    }
//    endregion

    public static void setVerbose(boolean verbose) {
        S3UploadService.VERBOSE = verbose;
    }
}