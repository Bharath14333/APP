package com.emergency.crisissense.model;

import java.io.Serializable;

public class NearbyPlace implements Serializable {
    private String placeId;
    private String name;
    private String type; // Hospital, Police Station, Fire Station
    private String distance;
    private String address;
    private String phone;
    private double latitude;
    private double longitude;

    public NearbyPlace() {
    }

    public NearbyPlace(String placeId, String name, String type, String distance, String address, String phone, double latitude, double longitude) {
        this.placeId = placeId;
        this.name = name;
        this.type = type;
        this.distance = distance;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
