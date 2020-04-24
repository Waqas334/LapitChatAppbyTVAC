package com.androidbull.firebasechatapp.model;

public class User {
    private String name;
    private String status;
    private String thumbnail;
    private boolean isOnline;

    public User() {
    }

    public User(String name, String status, String thumbnail,boolean isOnline) {
        this.name = name;
        this.status = status;
        this.thumbnail = thumbnail;
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
