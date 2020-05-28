package com.example.kimseongjin.realtime_fishing;

import net.daum.mf.map.api.MapPoint;

/**
 * Created by kimseongjin on 2017. 5. 25..
 */

public class FishingRecord {

    int fishNum;
    MapPoint.GeoCoordinate hook_point;
    float tidal_speed;
    float tidal_deg;
    float water_temp;
    String post_name;
    String hookTime;

    public FishingRecord(){}
    public FishingRecord(int num, MapPoint point, float speed, float deg, float temp, String p_name,  String time){
        this.fishNum = num;
        this.hook_point = point.getMapPointGeoCoord();
        this.tidal_speed = speed;
        this.tidal_deg = deg;
        this.water_temp = temp;
        this.post_name = p_name;
        this.hookTime = time;
    }

    public void setFishNum(int fishNum) {
        this.fishNum = fishNum;
    }
    public void setHook_point(MapPoint hook_point) {this.hook_point = hook_point.getMapPointGeoCoord();}
    public void setHookTime(String hookTime) {
        this.hookTime = hookTime;
    }
    public void setTidal_deg(float tidal_deg) {
        this.tidal_deg = tidal_deg;
    }
    public void setTidal_speed(float tidal_speed) {
        this.tidal_speed = tidal_speed;
    }
    public void setWater_temp(float water_temp) {
        this.water_temp = water_temp;
    }
    public void setPost_name(String post_name) {this.post_name = post_name;}

    public float getTidal_deg() {
        return tidal_deg;
    }
    public float getTidal_speed() {
        return tidal_speed;
    }
    public float getWater_temp() {
        return water_temp;
    }
    public int getFishNum() {
        return fishNum;
    }
    public MapPoint.GeoCoordinate getHook_point() {
        return hook_point;
    }
    public String getHookTime() {
        return hookTime;
    }
    public String getPost_name() {return post_name;}
}
