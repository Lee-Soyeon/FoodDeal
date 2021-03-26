package com.hankki.fooddeal.ui.chatting.chatDTO;

import java.util.Date;
import java.util.List;

public class Message {
    private String messageSenderUid;
    private String messageContent;
    private Date messageTime;
    private String messageType;
    private List<String> messageReadUserList;

    public Message() {
    }

    public Message(String messageSenderUid, String messageContent, Date messageTime, String messageType, List<String> messageReadUserList) {
        this.messageSenderUid = messageSenderUid;
        this.messageContent = messageContent;
        this.messageTime = messageTime;
        this.messageType = messageType;
        this.messageReadUserList = messageReadUserList;
    }

    public String getMessageSenderUid() {
        return messageSenderUid;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public Date getMessageTime() {
        return messageTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public List<String> getMessageReadUserList() {
        return messageReadUserList;
    }
}
