package com.emergency.crisissense.model;

public class FavoriteService {
    private String serviceId;
    private String name;
    private String category;
    private double latitude;
    private double longitude;
    private String address;
    private String phone;
    private long createdAt;

    // Required for Firestore
    public FavoriteService() {
    }

    public FavoriteService(String serviceId, String name, String category, double latitude, double longitude, 
                           String address, String phone, long createdAt) {
        this.serviceId = serviceId;
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
