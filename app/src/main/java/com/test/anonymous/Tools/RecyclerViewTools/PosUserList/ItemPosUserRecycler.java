package com.test.anonymous.Tools.RecyclerViewTools.PosUserList;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;

public class ItemPosUserRecycler {

    private String UID , name;
    private String selfiePath;//存放照片url
    private GeoPoint startGeo;//自身位置
    private GeoPoint endGeo;//每一位posUser位置
    private Double distance;

    public ItemPosUserRecycler(String UID, String name, String selfiePath, GeoPoint startGeo, GeoPoint endGeo) {
        this.UID = UID;
        this.name = name;
        this.selfiePath = selfiePath;
        this.startGeo = startGeo;
        this.endGeo = endGeo;
        this.distance= calDistance(startGeo.getLatitude() , startGeo.getLongitude() , endGeo.getLatitude() , endGeo.getLongitude());
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

    //取得距離
    private double calDistance(double startLatitude , double startLongitude , double endLatitude , double endLongitude){
        float [] distance = new float[10];
        Location.distanceBetween(startLatitude , startLongitude , endLatitude , endLongitude , distance);
        return distance[0]/1000;//公尺變公里
    }

    public Double getDistance() {
        return distance;
    }
}

