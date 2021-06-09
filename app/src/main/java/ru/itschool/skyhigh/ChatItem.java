package ru.itschool.skyhigh;

import java.util.Date;

public class ChatItem implements java.io.Serializable{
    public String chat_Type;
    public String chatName;
    public String text_lastMessage;
    private String lastMessageTime;
    public long lastConversation_ID;

    public ChatItem() {}
    public ChatItem(String chat_Type, String chatName, String text_lastMessage, String lastMessageTime, long lastConversation_ID) {
        this.chat_Type = chat_Type;
        this.chatName = chatName;
        this.text_lastMessage = text_lastMessage;

        this.lastMessageTime = lastMessageTime;
        this.lastConversation_ID = lastConversation_ID;
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

    public String getChat_Type() {
        return chat_Type;
    }

    public void setChat_Type(String chat_Type) {
        this.chat_Type = chat_Type;
    }

    public long getLastConversation_ID() {
        return lastConversation_ID;
    }

    public void setLastConversation_ID(long lastConversation_ID) {
        this.lastConversation_ID = lastConversation_ID;
    }
}

