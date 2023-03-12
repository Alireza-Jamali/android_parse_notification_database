package com.aeza.chat;

import android.graphics.Bitmap;

import com.github.bassaer.chatmessageview.model.IChatUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class User implements IChatUser {
    private int id;
    private String name;
    private Bitmap icon;

    public User(int id, String name, Bitmap icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    @Nullable
    @Override
    public Bitmap getIcon() {
        return icon;
    }

    @NonNull
    @Override
    public String getId() {
        return id + "";
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setIcon(@NonNull Bitmap bitmap) {
        icon = bitmap;
    }
}
