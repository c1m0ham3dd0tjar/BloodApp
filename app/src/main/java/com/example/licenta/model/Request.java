package com.example.licenta.model;

import java.util.List;

public class Request {

    private String bloodType;
    private int bloodUnitsNeeded;
    private String location;
    private String patientName;
    private int patientAge;
    private String patientGender;
    private String contactPhone;
    private List<String> compatibleBloodTypes;
    private String requiredUptoDate;
    private String requesterId;
    private String key;

    public Request() {
    }

    public Request(String bloodType, int bloodUnitsNeeded, String location, String patientName, int patientAge, String patientGender,
                   String contactPhone, List<String> compatibleBloodTypes, String requiredUptoDate,  String requesterId, String key) {
        this.bloodType = bloodType;
        this.bloodUnitsNeeded = bloodUnitsNeeded;
        this.location = location;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.contactPhone = contactPhone;
        this.compatibleBloodTypes = compatibleBloodTypes;
        this.requiredUptoDate = requiredUptoDate;
        this.requesterId = requesterId;
        this.key = key;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getBloodUnitsNeeded() {
        return bloodUnitsNeeded;
    }

    public void setBloodUnitsNeeded(int bloodUnitsNeeded) {
        this.bloodUnitsNeeded = bloodUnitsNeeded;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public List<String> getCompatibleBloodTypes() {
        return compatibleBloodTypes;
    }

    public void setCompatibleBloodTypes(List<String> compatibleBloodTypes) {
        this.compatibleBloodTypes = compatibleBloodTypes;
    }

    public String getRequiredUptoDate() {
        return requiredUptoDate;
    }

    public void setRequiredUptoDate(String requiredUptoDate) {
        this.requiredUptoDate = requiredUptoDate;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

// get Current user id bla request save in the firebase bla and then reciclervyew with requests