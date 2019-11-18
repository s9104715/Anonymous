package com.test.anonymous.Tools.RecyclerViewTools.TopicList;

public class ItemTopic {

    private String id;
    private String topic;
    private boolean isSelected;

    public ItemTopic(String id , String topic) {
        this.id = id;
        this.topic = topic;
        this.isSelected = false;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
