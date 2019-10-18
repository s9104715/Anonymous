package com.test.anonymous.Tools.RecyclerViewTools.HobbyList;

public class ItemHobby {

    private String hobby;
    private boolean isSelected;
    private boolean isOther;//是否為其他選項
    private String otherHobby;//其他選項上的文字

    public ItemHobby(String hobby, boolean isSelected) {
        this.hobby = hobby;
        this.isSelected = isSelected;
        if(hobby.isEmpty()){
            this.isOther = true;
            this.otherHobby = "";
        }else {
            this.isOther = false;
        }
    }

    public String getHobby() {
        return hobby;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isOther() {
        return isOther;
    }

    public String getOtherHobby() {
        return otherHobby;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setOtherHobby(String otherHobby) {
        this.otherHobby = otherHobby;
    }
}
