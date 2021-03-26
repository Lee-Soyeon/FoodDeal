package com.hankki.fooddeal.ux.recyclerview;


public class MessageItem {
    String userName;
    String message;
    String time;
    public MessageItem(String name, String msg, String t){
        userName = name;
        message = msg;
        time = t;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
