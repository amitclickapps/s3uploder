package test.s3uploader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.regions.Regions;
import com.s3.callback.S3Callback;
import com.s3.model.S3BucketData;
import com.s3.model.S3Credentials;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clickapps on 31/8/17.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.button)
    Button getBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button) {
            new S3Credentials("accessKey", "secretKey");
            new S3BucketData.Builder(this)
                    .setBucket("algarage")
                    .setBucketFolder("dev")
                    .setRegion(Regions.AP_SOUTHEAST_1.getName())
                    .setKey(new File("file"), "fileName")
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
        } else {
            Log.i(getLocalClassName(), "No clickHandled");
        }
    }
}
