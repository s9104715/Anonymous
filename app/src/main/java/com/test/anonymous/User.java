package com.test.anonymous;

public class User {

    private String name;
    private String gender;
    private boolean verify;//帳號有無啟用，必須通過信箱驗證信
    private String selfiePath;//存放照片url
    private String LOGIN_TYPE;//登入方法包含NORMAL_LOGIN , GOOGLE_LOGIN , FACEBOOK_LOGIN

    public User() {
    }

    public User(String name, String gender, boolean verify, String selfiePath, String LOGIN_TYPE) {
        this.name = name;
        this.gender = gender;
        this.verify = verify;
        this.selfiePath = selfiePath;
        this.LOGIN_TYPE = LOGIN_TYPE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isVerify() {
        return verify;
    }

    public String getSelfiePath() {
        return selfiePath;
    }

    public void setSelfiePath(String selfiePath) {
        this.selfiePath = selfiePath;
    }

    public String getLOGIN_TYPE() {
        return LOGIN_TYPE;
    }
}
