package com.test.anonymous.Tools.RecyclerViewTools.ChatList;

public class ItemChat {

    private int index;
    private String userUID;
    private String mySelfiePath;
    private String otherSelfiePath;
    private String text;
    private String time;

    public ItemChat(){
    }

    public ItemChat(int index, String userUID, String mySelfiePath, String otherSelfiePath, String text, String time) {
        this.index = index;
        this.userUID = userUID;
        this.mySelfiePath = mySelfiePath;
        this.otherSelfiePath = otherSelfiePath;
        this.text = text;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public String getUserUID() {
        return userUID;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getMySelfiePath() {
        return mySelfiePath;
    }

    public String getOtherSelfiePath() {
        return otherSelfiePath;
    }
}

