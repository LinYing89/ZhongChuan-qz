package com.bairock.zhongchuan.qz.bean;

public class Location {

    //经度
    private Double lng;
    //纬度
    private Double lat;

    public Location() {
    }

    public Location(Double lng, Double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
