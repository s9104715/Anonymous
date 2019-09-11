package com.test.anonymous;

public class ItemFriends {

    private String selfiePath;
    private String name;
    private String chatRoomID;

    public ItemFriends() {
    }

    public ItemFriends(String selfiePath, String name, String chatRoomID) {
        this.selfiePath = selfiePath;
        this.name = name;
        this.chatRoomID = chatRoomID;
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
