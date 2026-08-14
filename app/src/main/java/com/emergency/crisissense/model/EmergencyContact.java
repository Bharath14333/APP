package com.emergency.crisissense.model;

import java.io.Serializable;

public class EmergencyContact implements Serializable {
    private String contactId;
    private String name;
    private String number;
    private String type; // Police, Fire, Hospital, General
    private String relationship; // Spouse, Parent, Friend, Doctor, etc.

    // Required empty constructor for Firestore
    public EmergencyContact() {
    }

    public EmergencyContact(String contactId, String name, String number, String type, String relationship) {
        this.contactId = contactId;
        this.name = name;
        this.number = number;
        this.type = type;
        this.relationship = relationship;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
