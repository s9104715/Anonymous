package com.test.anonymous.Tools.RecyclerViewTools.FriendsList;

import com.google.firebase.Timestamp;

public class ItemFriends {

    private String userUID;
    private String selfiePath;
    private String name;
    private String chatRoomID;
    private String lastLine;
    private Timestamp lastTime;
    private int unreadLineNum;

    public ItemFriends() {
    }

    public ItemFriends(String userUID, String selfiePath, String name, String chatRoomID, String lastLine , Timestamp lastTime, int unreadLineNum) {
        this.userUID = userUID;
        this.selfiePath = selfiePath;
        this.name = name;
        this.chatRoomID = chatRoomID;
        this.lastLine = lastLine;
        this.lastTime = lastTime;
        this.unreadLineNum = unreadLineNum;
    }

    public String getUserUID() {
        return userUID;
    }

    public String getSelfiePath() {
        return selfiePath;
    }

    public String getName() {
        return name;
    }

    public String getChatRoomID() {
        return chatRoomID;
    }

    public String getLastLine() {
        return lastLine;
    }

    public Timestamp getLastTime() {
        return lastTime;
    }

    public int getUnreadLineNum() {
        return unreadLineNum;
    }

    public void setName(String name) {
        this.name = name;
    }
}
