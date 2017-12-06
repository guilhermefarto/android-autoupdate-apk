package autoupdateapk.android.guilherme.cleva.farto.autoupdateapk.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = UpdateTask.class.getName();

    public static final String APK_TMP_NAME = "update.apk";
    public static final String APK_TYPE = "application/vnd.android.package-archive";

    private Context context;

    private ProgressDialog progressDialog;

    public UpdateTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(String... urls) {
        try {
            String jsonResponse = this.getApkVersionsJSON(urls[0]);

            Log.i(TAG, "jsonResponse: " + jsonResponse);

            JSONObject versionsJson = (JSONObject) new JSONParser().parse(jsonResponse);

            String downloadApkUrl = String.valueOf(((JSONObject) versionsJson.get("versions")).get(urls[1]));

            Log.i(TAG, "downloadApkUrl: " + downloadApkUrl);

            // ### Uses external storage (SD Card memory)
            this.downloadExternalStorageAndInstallApkVersion(downloadApkUrl);

            // ### Uses internal storage (device memory)
            // this.downloadInternalStorageAndInstallApkVersion(downloadApkUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        progressDialog.dismiss();
    }

    private String getApkVersionsJSON(String versionsUrl) throws IOException {
        HttpURLConnection urlConnection = this.getHttpURLConnection(versionsUrl);

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();

        if (inputStream == null) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;

        while ((line = reader.readLine()) != null) {
            buffer.append(line + "\n");
        }

        return buffer.length() > 0 ? buffer.toString() : null;
    }

    private void downloadExternalStorageAndInstallApkVersion(String downloadUrl) throws IOException {
        HttpURLConnection urlConnection = this.getHttpURLConnection(downloadUrl);
        urlConnection.connect();

        String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + APK_TMP_NAME;

        File apkFile = new File(file);

        Log.i(TAG, "apkFile: " + apkFile);

        if (apkFile.exists()) {
            apkFile.delete();
        }

        FileOutputStream fos = new FileOutputStream(apkFile);

        this.downloadApk(urlConnection, fos);

        this.installApk(apkFile);
    }

    private void downloadInternalStorageAndInstallApkVersion(String downloadUrl) throws IOException {
        HttpURLConnection urlConnection = this.getHttpURLConnection(downloadUrl);
        urlConnection.connect();

        FileOutputStream fos = this.context.openFileOutput(APK_TMP_NAME, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);

        this.downloadApk(urlConnection, fos);

        File apkFile = new File(this.context.getFilesDir(), APK_TMP_NAME);

        Log.i(TAG, "apkFile: " + apkFile);

        this.installApk(apkFile);
    }

    private void downloadApk(HttpURLConnection urlConnection, FileOutputStream fos) throws IOException {
        InputStream is = urlConnection.getInputStream();

        byte[] dataBuffer = new byte[1024];
        int dataLength;
        while ((dataLength = is.read(dataBuffer)) != -1) {
            fos.write(dataBuffer, 0, dataLength);
        }

        fos.close();
        is.close();

        Log.i(TAG, "Download finished...");
    }

    private void installApk(File apkFile) {
        Log.i(TAG, "apkFile.uri: " + Uri.fromFile(apkFile));

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.fromFile(apkFile), APK_TYPE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    private HttpURLConnection getHttpURLConnection(String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Pragma", "no-cache");
        urlConnection.setRequestProperty("Cache-Control", "no-cache");
        urlConnection.setRequestProperty("Expires", "-1");

        return urlConnection;
    }

}
