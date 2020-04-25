package com.androidbull.firebasechatapp.model;

public class Conversation {

    private boolean seen;
    private long timestamp;
    private String lastMessage;

    public Conversation() {
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Conversation(boolean seen, long timestamp, String lastMessage) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
    }
}
