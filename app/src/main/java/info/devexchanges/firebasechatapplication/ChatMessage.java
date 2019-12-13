package info.devexchanges.firebasechatapplication;

import java.util.Date;

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private String messageUserId;
    private String color;
    private long messageTime;

    public ChatMessage() {

    }
    public ChatMessage(String messageText, String messageUser, String messageUserId, String color) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageUserId = messageUserId;
        this.color = color;
        this.messageTime = new Date().getTime();
    }


    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getMessageUserId() {
        return messageUserId;
    }

    public void setMessageUserId(String messageUserId) {
        this.messageUserId = messageUserId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
