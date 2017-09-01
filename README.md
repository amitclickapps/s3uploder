# S3UploadService 
Implementation of an IntentService that receives a File and information about an S3 bucket. The service handles the upload of said file to the bucket.

Usage
--------
Build an instance of S3BucketData and make a call to S3UploadService.upload():
```
         new S3Credentials("accessKey", "secretKey");
                     new S3BucketData.Builder(this)
                             .setBucket("algarage")
                             .setBucketFolder("dev")
                             .setRegion(Regions.AP_SOUTHEAST_1.getName())
                             .setKey(file, "fileName")
                             .setS3Callback(new S3Callback() {
                                 @Override
                                 public void onProgressChanged(int extra, long bytesCurrent, long bytesTotal) {
                                     Log.i(getLocalClassName(), "bytesCurrent = " + bytesCurrent + " bytesTotal = " + bytesTotal);
                                     float fpercent = ((bytesCurrent * 100) / bytesTotal);
                                     Log.i(getLocalClassName(), "fpercent = " + fpercent);
                                 }
         
                                 @Override
                                 public void onResult(boolean status, String uploadedUrl, S3BucketData s3BucketData) {
                                     Log.i(getLocalClassName(), "status = " + status + " uploadedUrl = " + uploadedUrl);
                                 }
                             })
                             .build();
```
where key is:
```
        key = file to upload
        fileName = Name of that file
```

keep in mind that the final URL will have the following format:
```
        finalUrl = "https://" + bucket + ".s3.amazonaws.com/"+bucketFolder + fileName;
```
Download
--------
Add the JitPack repository to your root build.gradle:[![](https://jitpack.io/v/amitclickapps/s3uploder.svg)](https://jitpack.io/#amitclickapps/s3uploder)


```groovy
	allprojects {
		repositories {
			maven { url "https://jitpack.io" }
		}
	}
```
Add the Gradle dependency:
```groovy
	dependencies {
		compile 'com.github.amitclickapps:s3uploder:1.0.3'
	}
```
Declare S3UploadService in your manifest:
```
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="$package_name">

    <application
        ...>
        
        ...
        
        <service
            android:name="com.amazonaws.mobileconnectors.s3.transferutility.TransferService"
            android:enabled="true" />
            
    </application>

</manifest>

```
