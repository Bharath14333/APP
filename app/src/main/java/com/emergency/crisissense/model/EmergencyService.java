package com.emergency.crisissense.model;

public class EmergencyService {
    private String serviceId;
    private String name;
    private String category; // Hospital, Police, Fire, Ambulance, Pharmacy, Shelter, Other
    private String address;
    private String phone;
    private double latitude;
    private double longitude;
    private boolean emergencyAvailable;
    private boolean isActive;
    private String source;

    // Default constructor required for Firestore
    public EmergencyService() {
    }

    public EmergencyService(String serviceId, String name, String category, String address, String phone, 
                            double latitude, double longitude, boolean emergencyAvailable, boolean isActive, String source) {
        this.serviceId = serviceId;
        this.name = name;
        this.category = category;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.emergencyAvailable = emergencyAvailable;
        this.isActive = isActive;
        this.source = source;
    }

    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isEmergencyAvailable() { return emergencyAvailable; }
    public void setEmergencyAvailable(boolean emergencyAvailable) { this.emergencyAvailable = emergencyAvailable; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
