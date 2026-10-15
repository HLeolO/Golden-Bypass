// language: Java
// file: app/src/main/java/com/golden/bypass/InstallResultReceiver.java
package com.golden.bypass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;
import android.widget.Toast;

public class InstallResultReceiver extends BroadcastReceiver {
    private static final String TAG = "GoldenBypass";

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                PackageInstaller.STATUS_FAILURE);
        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

        if (status == PackageInstaller.STATUS_SUCCESS) {
            Log.i(TAG, "Payload installed successfully");
            Toast.makeText(context, "Payload installed", Toast.LENGTH_SHORT).show();

            // Lança o payload instalado
            Intent launch = context.getPackageManager()
                    .getLaunchIntentForPackage("com.golden.payload");
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launch);
            }
        } else {
            Log.e(TAG, "Install failed: " + status + " — " + message);
            Toast.makeText(context, "Install failed: " + message, Toast.LENGTH_LONG).show();
        }
    }
}
