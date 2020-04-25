package com.androidbull.firebasechatapp.model;

public class Request {
    private String request_type;

    public Request() {
    }

    public Request(String request_type) {
        this.request_type = request_type;
    }

    public String getRequest_type() {
        return request_type.equals("sent") ? "Request Sent" : "Request Received";
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public boolean isSent() {
        return request_type.equals("sent") ? true : false;
    }
}
