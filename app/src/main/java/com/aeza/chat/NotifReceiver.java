package com.aeza.chat;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.bassaer.chatmessageview.model.Message;
import com.parse.ManifestInfo;
import com.parse.PLog;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import androidx.core.app.NotificationCompat;

public class NotifReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "aeza";
    public static final String intentAction = "com.parse.push.intent.RECEIVE";
    private static String channel = "aeza";
    public static final int NOTIFICATION_ID = 45;

    public static String title;
    public static String alert;

    public static boolean hasMsg() {
        return title != null;
    }

    public static void erase() {
        title = alert = null;
    }

    public static void setChannel(String newChannel) {
        channel = newChannel;
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        String pushDataStr = intent.getStringExtra(KEY_PUSH_DATA);
        if (pushDataStr == null) {
            PLog.e(TAG, "Can not get push data from intent.");
            return;
        }
        PLog.v(TAG, "Received push data: " + pushDataStr);

        JSONObject pushData = null;
        try {
            pushData = new JSONObject(pushDataStr);
        } catch (JSONException e) {
            PLog.e(TAG, "Unexpected JSONException when receiving push data: ", e);
        }
        boolean isForeground = isAppOnForeground(context);

        // If the push data includes an action string, that broadcast intent is fired.
        String action = null;
        if (pushData != null) {
            action = pushData.optString("action", null);
            title = pushData.optString("title", ManifestInfo.getDisplayName(context));
            if (!title.equals("aeza"))
                MainActivity.instance.lastName = title;
            alert = pushData.optString("alert", "Notification received.");
            if (isForeground && !MainActivity.instance.lastMsg.equals(alert)) {
                MainActivity.instance.lastMsg = alert;
                MainActivity.instance.receiveMsg(title, alert);
            }
        }
        if (action != null) {
            Bundle extras = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(extras);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }


        final NotificationCompat.Builder notificationBuilder = getNotification(context, intent);

        Notification notification = null;
        if (notificationBuilder != null) {
            notification = notificationBuilder.build();
        }

        if (notification != null && !isForeground) {
            // Fire off the notification
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Pick an id that probably won't overlap anything
            int notificationId = (int) System.currentTimeMillis();

            try {
                nm.notify(notificationId, notification);
            } catch (SecurityException e) {
                // Some phones throw an exception for unapproved vibration
                notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                nm.notify(notificationId, notification);
            }
        }
    }

    //not gonna work for parse, we got that from firebase docs
    private void processPush(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        if (action.equals(intentAction)) {
            String channel = intent.getExtras().getString("com.parse.Channel");
            try {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                // Iterate the parse keys if needed

                Log.d(TAG, "title" + json.get("title"));
                Log.d(TAG, "alert" + json.get("alert"));

                Iterator<String> itr = json.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    String value = json.getString(key);
                    Log.d(TAG, "..." + key + " => " + value);
                }

                title = json.get("title").toString();
                alert = json.get("alert").toString();

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channel)
                        .setContentTitle(json.get("title").toString()).setContentText(json.get("alert").toString());
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (JSONException ex) {
                Log.d(TAG, "JSON failed!");
            }
        }
    }

    public boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
