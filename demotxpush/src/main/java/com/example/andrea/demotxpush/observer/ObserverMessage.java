package com.example.andrea.demotxpush.observer;

public class ObserverMessage {

    public static final int BROADCAST_SET_TAG_SUCCESS = 1;
    public static final int BROADCAST_DELETE_TAG_SUCCESS = 2;
    public static final int BROADCAST_SET_TAG_FAIL= 3;
    public static final int BROADCAST_DELETE_TAG_FAIL = 4;
    public static final int BROADCAST_MSG = 5;
    public static final int BROADCAST_NOTIFICATION = 6;
    public static final int BROADCAST_NOTIFICATION_CLICK = 7;

    private int messageType;

    private String tagName;

    private long msgId;

    private int notificationId;

    private String title;

    private String content;

    private int notificationClickActionType;

    private int notificationActionType;

    private String notificationActivity;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNotificationClickActionType() {
        return notificationClickActionType;
    }

    public void setNotificationClickActionType(int notificationClickActionType) {
        this.notificationClickActionType = notificationClickActionType;
    }

    public int getNotificationActionType() {
        return notificationActionType;
    }

    public void setNotificationActionType(int notificationActionType) {
        this.notificationActionType = notificationActionType;
    }

    public String getNotificationActivity() {
        return notificationActivity;
    }

    public void setNotificationActivity(String notificationActivity) {
        this.notificationActivity = notificationActivity;
    }
}
