package com.androidbull.firebasechatapp.model;

public class Message {
    private String message;
    private boolean seen;
    private String type;
    private long time;
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Message() {
    }

    public Message(String message, boolean seen, String type, long time) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
