package co.realtime.plugins.android.cordovapush;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class OrtcPushHandlerActivity extends Activity{
    private static String TAG = "OrtcPushHandlerActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

        boolean isPushPluginActive = OrtcPushPlugin.isActive();
        if (!isPushPluginActive) {
            forceMainActivityReload();
        }

        processPushBundle();

        finish();
    }

    private void processPushBundle()
    {
        Bundle extras = getIntent().getExtras();

        if (extras != null)	{
            Bundle originalExtras = extras.getBundle("pushBundle");

            originalExtras.putBoolean("foreground", false);
            OrtcPushPlugin.sendExtras(originalExtras);
        }
    }

    private void forceMainActivityReload()
    {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }

}
