package com.test.anonymous;

public class ItemFriends {

    private String selfiePath;
    private String name;

    public ItemFriends() {
    }

    public ItemFriends(String selfiePath, String name) {
        this.selfiePath = selfiePath;
        this.name = name;
    }

    public String getSelfiePath() {
        return selfiePath;
    }

    public String getName() {
        return name;
    }
}
