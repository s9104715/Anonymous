package com.test.anonymous.Tools.RecyclerViewTools.InvitationList;

import com.google.firebase.Timestamp;

public class ItemInvitation {

    private String userUID , selfiePath ,name , info;
    private String distance;
    private Timestamp time;
    private boolean isRead;

    public ItemInvitation(String userUID, String selfiePath, String name, String info, String distance, Timestamp time, boolean isRead) {
        this.userUID = userUID;
        this.selfiePath = selfiePath;
        this.name = name;
        this.info = info;
        this.distance = distance;
        this.time = time;
        this.isRead = isRead;
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

    public String getInfo() {
        return info;
    }

    public String getDistance() {
        return distance;
    }

    public Timestamp getTime() {
        return time;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
