package autoupdateapk.android.guilherme.cleva.farto.autoupdateapk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import autoupdateapk.android.guilherme.cleva.farto.autoupdateapk.tasks.UpdateTask;
import autoupdateapk.android.guilherme.cleva.farto.autoupdateapk.utils.ActivityUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    public static final String DOWNLOAD_VERSIONS_JSON_URL = "https://github.com/guilhermefarto/android-autoupdate-apk/raw/master/autoupdate-apk-versions/versions.json";

    public static final String RESTORED_APK_VERSION = "1.0-old";
    public static final String LATEST_APK_VERSION = "2.0-rc";

    public static final int RC_LATEST_APK_VERSION = 1000;
    public static final int RC_RESTORED_APK_VERSION = 1001;

    private TextView lblVersionName;
    private Button btnUpdate;
    private Button btnRestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);

        lblVersionName = findViewById(R.id.lblVersionName);
        btnUpdate = findViewById(R.id.btnUpdate);

        try {
            lblVersionName.setText(ActivityUtil.getVersionName(MainActivity.this));
        } catch (PackageManager.NameNotFoundException e) {
            lblVersionName.setText("Error: VersionName not verified");
            e.printStackTrace();
        }

        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                update();
            }
        });

        /* btnRestore = findViewById(R.id.btnRestore);

        btnRestore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                restore();
            }
        }); */
    }

    private void update() {
        if (this.isStoragePermissionGranted(RC_LATEST_APK_VERSION)) {
            new UpdateTask(MainActivity.this).execute(DOWNLOAD_VERSIONS_JSON_URL, LATEST_APK_VERSION);
        }
    }

    private void restore() {
        if (this.isStoragePermissionGranted(RC_RESTORED_APK_VERSION)) {
            new UpdateTask(MainActivity.this).execute(DOWNLOAD_VERSIONS_JSON_URL, RESTORED_APK_VERSION);
        }
    }

    public boolean isStoragePermissionGranted(int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission is granted");

                return true;
            } else {
                Log.i(TAG, "Permission is revoked");

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);

                return false;
            }
        } else {
            Log.i(TAG, "Permission is granted");

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission: " + permissions[0] + " was " + (grantResults[0] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

            if (MainActivity.RC_LATEST_APK_VERSION == requestCode) {
                this.update();
            } else if (MainActivity.RC_LATEST_APK_VERSION == requestCode) {
                this.restore();
            }
        }
    }

}