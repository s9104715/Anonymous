package com.test.anonymous.Tools.RecyclerViewTools.TopicLibList;

public class ItemTopicLib {

    private String id;
    private String UID;
    private String name;
    private String selfiePath;
    private String topic;
    private int downloadTime;
    private boolean hasDownloaded;

    public ItemTopicLib(String id, String UID, String name, String selfiePath, String topic, int downloadTime) {
        this.id = id;
        this.UID = UID;
        this.name = name;
        this.selfiePath = selfiePath;
        this.topic = topic;
        this.downloadTime = downloadTime;
        this.hasDownloaded = false;
    }

    public String getId() {
        return id;
    }

    public String getUID() {
        return UID;
    }

    public String getName() {
        return name;
    }

    public String getSelfiePath() {
        return selfiePath;
    }

    public String getTopic() {
        return topic;
    }

    public int getDownloadTime() {
        return downloadTime;
    }

    public boolean isHasDownloaded() {
        return hasDownloaded;
    }

    public void addDownloadTime() {
        this.downloadTime = downloadTime + 1;
    }

    public void setHasDownloaded(boolean hasDownloaded) {
        this.hasDownloaded = hasDownloaded;
    }
}
