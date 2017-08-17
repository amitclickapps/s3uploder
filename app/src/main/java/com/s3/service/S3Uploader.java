package com.s3.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.internal.TransferProgressUpdatingListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.s3.BuildConfig;
import com.s3.R;
import com.s3.model.S3BucketData;
import com.s3.model.S3Credentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by amit on 16/8/17.
 */

public class S3Uploader {
    private S3Uploader() {
    }


    public S3Uploader(@NonNull S3BucketData s3BucketData) {
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
            final String url = String.format(s3BucketData.getContext().getString(R.string.s3_format_url), s3BucketData.getBucket(), s3BucketData.getBucketFolder(), s3BucketData.getKey().getName());
            private long uploadStartTime;

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
                        if (s3BucketData.isDeleteAfterUse()) {
                            s3BucketData.getKey().delete();
                        }
                        s3BucketData.getS3Callback().onResult(true, url, s3BucketData);
                        break;
                    case CANCELED:
                    case FAILED:
                        s3BucketData.getS3Callback().onResult(false, state.toString(), s3BucketData);
                        break;
                    case IN_PROGRESS:
                        uploadStartTime = System.currentTimeMillis();
                        break;
                    default:
//                        s3BucketData.getS3Callback().onResult(false, state.toString(), s3BucketData);
                        break;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (s3BucketData.getS3Callback() == null) {
                    return;
                }
                long uploadDurationMillis = System.currentTimeMillis() - uploadStartTime;
                int bytesPerSecond = (int) (s3BucketData.getKey().length() / (uploadDurationMillis / 1000.0));
                if (BuildConfig.DEBUG) {
                    double fileSize = s3BucketData.getKey().length() / 1000.0;
                    double uploadDuration = uploadDurationMillis;
                    double uploadSpeed = bytesPerSecond / 1000.0;
                    Log.i(getClass().getSimpleName(), String.format(s3BucketData.getContext().getString(R.string.s3_format_uploaded), fileSize, uploadDuration, uploadSpeed));
                }
                s3BucketData.getS3Callback().onProgressChanged(id, observer.getBytesTransferred(), observer.getBytesTotal());
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
        if (!TextUtils.isEmpty(s3BucketData.getRegion())) {
            s3.setRegion(Region.getRegion(Regions.fromName(s3BucketData.getRegion())));
        }
        return new TransferUtility(s3, s3BucketData.getContext());
    }
//    endregion

    //    region PutObjectRequest creation
    private PutObjectRequest buildPor(final S3BucketData s3BucketData) {

        final String bucket = s3BucketData.getBucket() + "/" + s3BucketData.getBucketFolder();
        final File file = s3BucketData.getKey();
        final boolean deleteFileAfter = s3BucketData.isDeleteAfterUse();
        ObjectMetadata omd = new ObjectMetadata();
        String contentType = getMimeType(file.getPath());
        if (!TextUtils.isEmpty(contentType)) {
            omd.setContentType(contentType);
        }
        omd.setContentLength(file.length());
        PutObjectRequest por = new PutObjectRequest(
                bucket, file.getName(), file);
        por.setMetadata(omd);
        por.setGeneralProgressListener(new ProgressListener() {
            final String url = String.format(s3BucketData.getContext().getString(R.string.s3_format_url), s3BucketData.getBucket(), s3BucketData.getBucketFolder(), file.getPath());
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
                            Log.i(getClass().getSimpleName(), String.format(s3BucketData.getContext().getString(R.string.s3_format_uploaded), fileSize, uploadDuration, uploadSpeed));
                        }

                        if (deleteFileAfter) {
                            file.delete();
                        }
                    } else if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        if (BuildConfig.DEBUG) {
                            Log.e(getClass().getSimpleName(), String.format(s3BucketData.getContext().getString(R.string.s3_format_upload_failed), url));
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

    // url = file path or whatever suitable URL you want.
    private
    @Nullable
    String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
