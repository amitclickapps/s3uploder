# S3UploadService [android]
Implementation of an IntentService that receives a File and information about an S3 bucket. The service handles the upload of said file to the bucket.

Usage
--------
Build an instance of S3BucketData and make a call to S3UploadService.upload():
```java
         S3Credentials s3Credentials = new S3Credentials("accessKey", "secretKey");
         S3BucketData s3BucketData = new S3BucketData.Builder()
                        .setCredentials(s3Credentials)
                        .setBucket("bucket")
                        .setKey(new File("adfasfd"))
                        .build();

         S3UploadService.upload(context,s3BucketData,callback);
```
where key is:
```java
        key = file
```

keep in mind that the final URL will have the following format:
```java
        finalUrl = "https://" + bucket + ".s3.amazonaws.com/" + key;
```

Callback
--------
S3Callback is an optional parameter in the upload() method. If not null, it should contain a String indicating what action will be broadcasted after upload completion. Optionally, you can add a Serializable to receive extra data

```java
       S3BroadCast s3Callback = new S3BroadCast("com.example.android.S3_UPLOAD_COMPLETED", extra, new S3BroadCast.S3Callback() {
                   @Override
                   public void onProgressChanged(Serializable extra, long bytesCurrent, long bytesTotal) {

                   }
               });
```
If s3Callback is provided to the upload() method, after the upload is completed, the following lines will be executed inside S3UploadService:
```java
        Intent intent = new Intent(s3Callback.getActionCallback());
        intent.putExtra(EXTRA_SERIALIZABLE, s3Response);
        sendBroadcast(intent);
```
So if we have a BroadcastReceiver capturing the specified action, it will receive the broadcast message along with the string "Awesome message"

Download
--------
Add the JitPack repository to your root build.gradle:

```groovy
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```
Add the Gradle dependency:
```groovy
	dependencies {
		compile 'com.github.amitclickapps:s3uploder:1.0.0'
	}
```
Declare S3UploadService in your manifest:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="$package_name">

    <application
        ...>
        
        ...
        
        <service
            android:name="com.s3.service.S3UploadService"
            android:exported="false" />
    </application>

</manifest>

```
