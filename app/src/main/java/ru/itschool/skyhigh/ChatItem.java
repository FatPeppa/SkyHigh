package ru.itschool.skyhigh;

import java.util.Date;

public class ChatItem {
    public String chatName;
    public String text_lastMessage;
    private String lastMessageTime;

    public ChatItem() {}
    public ChatItem(String chatName, String text_lastMessage, String lastMessageTime) {
        this.chatName = chatName;
        this.text_lastMessage = text_lastMessage;

        this.lastMessageTime = lastMessageTime;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getText_lastMessage() {
        return text_lastMessage;
    }

    public void setText_lastMessage(String text_lastMessage) {
        this.text_lastMessage = text_lastMessage;
    }

    public String getLastMessageTime() {

        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}

