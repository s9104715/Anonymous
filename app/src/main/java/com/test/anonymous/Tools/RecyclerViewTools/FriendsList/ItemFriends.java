package com.test.anonymous.Tools.RecyclerViewTools.FriendsList;

public class ItemFriends {

    private String userUID;
    private String selfiePath;
    private String name;
    private String chatRoomID;
    private String lastLine;
    private int unreadLineNum;

    public ItemFriends() {
    }

    public ItemFriends(String userUID, String selfiePath, String name, String chatRoomID, String lastLine, int unreadLineNum) {
        this.userUID = userUID;
        this.selfiePath = selfiePath;
        this.name = name;
        this.chatRoomID = chatRoomID;
        this.lastLine = lastLine;
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

    public int getUnreadLineNum() {
        return unreadLineNum;
    }
}
