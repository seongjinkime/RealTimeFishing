package com.example.kimseongjin.realtime_fishing;

import java.util.ArrayList;

/**
 * Created by kimseongjin on 2017. 5. 10..
 */

public class Weather_Data {
    private String icon;
    private int humidity;
    private float temp;
    private float windSpeed;
    private float windDeg;
    private float waterTemp;
    private float tidalSpeed;
    private float tidalDeg;
    private String lunar_Date;
    private String tide_time;
    private String oceanPostName;
    private String preTidePostName;
    private ArrayList<String []> pre_TideInfo;

    public void setIcon(String icon) {
        this.icon = icon;
    }
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
    public void setTemp(float temp) {
        this.temp = temp;
    }
    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }
    public void setWindDeg(float windDeg) {
        this.windDeg = windDeg;
    }
    public void setWaterTemp(float waterTemp) {
        this.waterTemp = waterTemp;
    }
    public void setTidalSpeed(float tidalSpeed) {
        this.tidalSpeed = tidalSpeed;
    }
    public void setTidalDeg(float tidalDeg) {
        this.tidalDeg = tidalDeg;
    }
    public void setLunar_Date(String lunar_Date) {this.lunar_Date = lunar_Date;}
    public void setTide_time(String tide_time) {this.tide_time = tide_time;}
    public void setOceanPostName(String oceanPostName) {this.oceanPostName = oceanPostName;}
    public void setPre_TideInfo(ArrayList<String[]> pre_TideInfo) {this.pre_TideInfo = pre_TideInfo;}
    public void setPreTidePostName(String preTidePostName) {this.preTidePostName = preTidePostName;}

    public String getIcon() {
        return icon;
    }
    public int getHumidity() {
        return humidity;
    }
    public float getTemp() {
        return temp;
    }
    public float getWindSpeed() {
        return windSpeed;
    }
    public float getWindDeg() {
        return windDeg;
    }
    public float getWaterTemp() {
        return waterTemp;
    }
    public float getTidalSpeed() {
        return tidalSpeed;
    }
    public float getTidalDeg() {
        return tidalDeg;
    }
    public String getLunar_Date() {return lunar_Date;}
    public String getTide_time() {return tide_time;}
    public ArrayList<String[]> getPre_TideInfo() {return pre_TideInfo;}
    public String getOceanPostName() {return oceanPostName;}
    public String getPreTidePostName() {return preTidePostName;}
}
