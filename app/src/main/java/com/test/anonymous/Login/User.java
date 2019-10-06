package com.test.anonymous.Login;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;

public class User {

    //login
    private String name;
    private String gender;
    private int age;
    private boolean verify;//帳號有無啟用，必須通過信箱驗證信
    private String selfiePath;//存放照片url
    private String LOGIN_TYPE;//登入方法包含NORMAL_LOGIN , GOOGLE_LOGIN , FACEBOOK_LOGIN

    public User() {
    }

    //for login
    public User(String name, String gender, int age ,  boolean verify, String selfiePath, String LOGIN_TYPE) {
        this.name = name;
        this.gender = gender;
        this.age = age;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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
