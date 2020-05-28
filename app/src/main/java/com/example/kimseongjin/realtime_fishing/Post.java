package com.example.kimseongjin.realtime_fishing;

/**
 * Created by kimseongjin on 2017. 5. 10..
 */

public class Post {
    String  id;
    double latitude;
    double longtitude;
    String datatype;
    String name;

    public void setId(String id) {
        this.id = id;
    }
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongtitude() {
        return longtitude;
    }
    public String getId() {
        return id;
    }
    public String getDatatype() {
        return datatype;
    }
    public String getName() {
        return name;
    }
}
