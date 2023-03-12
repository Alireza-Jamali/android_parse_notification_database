package com.aeza.chat;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.aeza.parse.R;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private ChatView mChatView;
    private int id = new Random().nextInt();
    public static MainActivity instance;
    SharedPreferences sh;
    public String name;
    public String lastName = "";
    User aeza;

    Map<String, User> users = new HashMap<>();
    List<String> objectIds = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Notif.initParse(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aeza = new User(id, "aeza", BitmapFactory.decodeResource(getResources(), R.drawable.aeza));
        users.put("aeza", aeza);
        sh = getSharedPreferences(getPackageName() + ".prefs", MODE_PRIVATE);
        instance = this;
        if ((name = sh.getString("name", null)) == null) {
            new NameFrag().show(getSupportFragmentManager(), "name_frag");
        } else
            setup();
    }

    public void setup() {
        mChatView = (ChatView) findViewById(R.id.chat_view);
        //Set UI parameters if you need
        mChatView.setRightBubbleColor(ContextCompat.getColor(this, com.github.bassaer.chatmessageview.R.color.green500));
        mChatView.setLeftBubbleColor(Color.WHITE);
        mChatView.setBackgroundColor(Color.parseColor("#D7010C01"));
        mChatView.setSendButtonColor(ContextCompat.getColor(this, com.github.bassaer.chatmessageview.R.color.cyan900));
        mChatView.setSendIcon(com.github.bassaer.chatmessageview.R.drawable.ic_action_send);
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.BLACK);
        mChatView.setUsernameTextColor(Color.WHITE);
        mChatView.setSendTimeTextColor(Color.WHITE);
        mChatView.setDateSeparatorColor(Color.WHITE);
        mChatView.setInputTextHint("new message...");
        mChatView.setMessageMarginTop(5);
        mChatView.setMessageMarginBottom(5);
        mChatView.setOnClickSendButtonListener(view -> {
            String msg = mChatView.getInputText();
            Message message = new Message.Builder()
                    .setUser(getUser(name))
                    .setRight(true)
                    .setText(msg)
                    .hideIcon(false)
                    .build();
            if (name.equals("aeza") && (lastName == null || lastName.isEmpty() || lastName.equals("aeza")))
                Toast.makeText(this, "change last name", Toast.LENGTH_SHORT).show();
            else
                Notif.sendMsg(name, msg, !name.equals("aeza") ? "aeza" : lastName);
            mChatView.send(message);
            mChatView.setInputText("");

            ParseObject send = new ParseObject("DawnChat");
            send.put("name", name);
            send.put("msg", msg);
            send.saveInBackground();

        });
        mChatView.setOnBubbleLongClickListener(v -> new NameFrag().show(getSupportFragmentManager(), "name_frag"));
        mChatView.setOnIconLongClickListener(v -> new NameFrag().setLastName().show(getSupportFragmentManager(), "name_frag2"));

        Notif.subscribeToChannels(Notif.senderId, Arrays.asList("online", name));
        Notif.sendMsg("aeza", name + " is online", "online");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("DawnChat");
        query.addAscendingOrder("createdAt");
        query.findInBackground((send, e) -> {
            if (e == null) {
                for (ParseObject data : send) {
                    String name = data.getString("name");
                    if (!name.equals("aeza")) lastName = name;
                    String msg = data.getString("msg");
                    objectIds.add(data.getObjectId());
                    Message message = new Message.Builder()
                            .setUser(getUser(name))
                            .setRight(name.equals(this.name))
                            .setText(msg)
                            .hideIcon(false)
                            .build();
                    mChatView.send(message);
                }
            }
        });
    }


    private User getUser(String name) {
        final User user;
        if (users.containsKey(name)) {
            user = users.get(name);
        } else {
            user = new User(new Random().nextInt(), name, BitmapFactory.decodeResource(getResources(), AvatarPicker.getRandomAvatar()));
            users.put(name, user);
        }
        return user;
    }

    public void receiveMsg(String name, String alert) {
        Message receivedMessage = new Message.Builder()
                .setUser(getUser(name))
                .setRight(false)
                .setText(alert)
                .build();
        mChatView.receive(receivedMessage);
    }

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 15 * 1000; //Delay for 15 seconds.  One second = 1000 milliseconds.


    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("DawnChat");
                query.addAscendingOrder("createdAt");
                query.findInBackground((send, e) -> {
                    if (e == null) {
                        for (ParseObject data : send) {
                            if (!objectIds.contains(data.getObjectId())) {
                                String name = data.getString("name");
                                if (!name.equals("aeza")) lastName = name;
                                String msg = data.getString("msg");
                                objectIds.add(data.getObjectId());
                                Message message = new Message.Builder()
                                        .setUser(getUser(name))
                                        .setRight(name.equals(MainActivity.this.name))
                                        .setText(msg)
                                        .hideIcon(false)
                                        .build();
                                mChatView.send(message);
                            }
                        }
                    }
                });
                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

// If onPause() is not included the threads will double up when you
// reload the activity

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
}