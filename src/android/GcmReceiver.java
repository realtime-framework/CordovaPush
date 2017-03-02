package co.realtime.plugins.android.cordovapush;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Random;

import ibt.ortc.extensibility.GcmOrtcBroadcastReceiver;

public class GcmReceiver extends GcmOrtcBroadcastReceiver {

    private static final String TAG = "GcmReceiver";

    public GcmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract the payload from the message
        Bundle extras = intent.getExtras();
        if (extras != null) {
            // if we are in the foreground, just surface the payload, else post it to the statusbar
            if (OrtcPushPlugin.isInForeground()) {
                extras.putBoolean("foreground", true);
                OrtcPushPlugin.sendExtras(extras, 0);
            } else {
                extras.putBoolean("foreground", false);
                // Send a notification if there is a message
                if (extras.getString("M") != null && extras.getString("M").length() != 0) {
                    createNotification(context, extras);
                }
            }
        }
    }

    public void createNotification(Context context, Bundle extras)
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(context);

        Intent notificationIntent = new Intent(context, OrtcPushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("pushBundle", extras);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int defaults = Notification.DEFAULT_ALL;

        if (extras.getString("defaults") != null) {
            try {
                defaults = Integer.parseInt(extras.getString("defaults"));
            } catch (NumberFormatException e) {}
        }

        String channel = extras.getString("C");
        String message = extras.getString("message");

        int largeIcon = getIcon(context, "large_notification_icon");

        Bitmap appIcon = BitmapFactory.decodeResource(context.getResources(), largeIcon);

        int color = getResource(context, "notification_color", "color");

        int smallIcon = getIcon(context, "small_notification_icon");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(defaults)
                        .setPriority(getAppPriority(context))
                        .setLargeIcon(appIcon)
                        .setSmallIcon(smallIcon)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(context.getString(context.getApplicationInfo().labelRes))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);


        if (color != 0){
            mBuilder.setColor(ContextCompat.getColor(context, color));
        }

        if (message != null) {
            mBuilder.setContentText(message);
        } else {
            mBuilder.setContentText("<missing message content>");
        }

        int notId = 0;

        try {
            notId = new Random().nextInt();
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID: " + e.getMessage());
        }
        catch(Exception e) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID" + e.getMessage());
        }

        mNotificationManager.notify(appName, notId, mBuilder.build());
    }

    private static int getIcon(Context context, String name){
        int imageResource;
        String uri = "drawable/" + name;
        imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

        if (imageResource == 0){
            imageResource = context.getApplicationInfo().icon;
        }
        return imageResource;
    }

    private static int getResource(Context context, String name, String type){
        int resource;
        resource = context.getResources().getIdentifier(name, type, context.getPackageName());
        return resource;
    }

    private static String getAppName(Context context)
    {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String)appName;
    }

    public static int getAppPriority(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int val = preferences.getInt("APP_PRIORITY", 0);
        return val;
    }

    public static void setAppPriority(Context context, int priority){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("APP_PRIORITY", priority);
        editor.apply();
    }

}
