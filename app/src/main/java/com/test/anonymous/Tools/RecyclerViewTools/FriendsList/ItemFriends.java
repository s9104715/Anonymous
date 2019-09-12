package com.test.anonymous.Tools.RecyclerViewTools.FriendsList;

public class ItemFriends {

    private String userUID;
    private String selfiePath;
    private String name;
    private String chatRoomID;

    public ItemFriends() {
    }

    public ItemFriends(String userUID, String selfiePath, String name, String chatRoomID) {
        this.userUID = userUID;
        this.selfiePath = selfiePath;
        this.name = name;
        this.chatRoomID = chatRoomID;
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
}
