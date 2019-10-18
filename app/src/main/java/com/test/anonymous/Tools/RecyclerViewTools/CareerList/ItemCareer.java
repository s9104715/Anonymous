package com.test.anonymous.Tools.RecyclerViewTools.CareerList;

public class ItemCareer {

    private String career;
    private boolean isSelected;
    private boolean isOther;//是否為其他選項
    private String otherCareer;//其他選項上的文字

    public ItemCareer(String career, boolean isSelected) {
        this.career = career;
        this.isSelected = isSelected;
        if(career.isEmpty()){
            this.isOther = true;
            this.otherCareer = "";
        }else {
            this.isOther = false;
        }
    }

    public String getCareer() {
        return career;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isOther() {
        return isOther;
    }

    public void setOtherCareer(String otherCareer) {
        this.otherCareer = otherCareer;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getOtherCareer() {
        return otherCareer;
    }
}
