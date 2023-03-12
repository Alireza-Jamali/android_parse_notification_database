package com.aeza.chat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseInstallation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notif extends Application {
    static public String back4app_app_id = "TODO ADD YOUR OWN";
    static public String back4app_client_key = "TODO ADD YOUR OWN";
    static public String back4app_server_url = "https://parseapi.back4app.com/";
    static public String senderId = "TODO ADD YOUR OWN";

    public static void initParse(Context ctx) {
//        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        Parse.initialize(new Parse.Configuration.Builder(ctx)
                .applicationId(back4app_app_id)
                .clientKey(back4app_client_key)
                .server(back4app_server_url)
                .build());
    }

    public static void subscribeToChannels(String senderId, List<String> channels) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", senderId);
        installation.put("channels", channels);
        installation.saveInBackground().continueWithTask(task -> {
            if (task.isFaulted())
                task.getError().printStackTrace();
            return task;
        });
    }

    public static void sendMsg(String title, String msg, String channel) {
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("alert", msg);
        params.put("channel", channel);
        ParseCloud.callFunctionInBackground("push", params)
                .continueWithTask(task -> {
                    if (task.isFaulted())
                        task.getError().printStackTrace();
                    return task;
                });
    }
//    Cloud code must be:
//    Parse.Cloud.define("push", (request) => {
//
//        return Parse.Push.send({
//                channels: [request.params.channel],
//        data: {
//            title: request.params.title,
//                    alert: request.params.alert,
//        }
//    }, { useMasterKey: true });
//    });
}
