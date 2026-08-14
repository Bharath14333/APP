package com.emergency.crisissense.model;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role; // citizen, volunteer, responder, admin
    private String profileImage;
    private String address;
    private Date createdDate;

    // Required empty constructor for Firestore
    public User() {
    }

    public User(String userId, String name, String email, String phone, String role, String profileImage, String address) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.profileImage = profileImage;
        this.address = address;
        this.createdDate = new Date();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
