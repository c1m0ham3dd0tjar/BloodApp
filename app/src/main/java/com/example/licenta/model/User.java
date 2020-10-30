package com.example.licenta.model;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

public class User {

    private String email, name, phone, bloodType, RH;
    private boolean isAvailable, isNotUnderAge;
    private Location location;

    public User() {}

    public User(String email, String name, String phone, String bloodType, String RH, boolean isAvailable, boolean isNotUnderAge) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.bloodType = bloodType;
        this.RH = RH;
        this.isAvailable = isAvailable;
        this.isNotUnderAge = isNotUnderAge;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getRH() {
        return RH;
    }

    public void setRH(String RH) {
        this.RH = RH;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isNotUnderAge() {
        return isNotUnderAge;
    }

    public void setNotUnderAge(boolean notUnderAge) {
        isNotUnderAge = notUnderAge;
    }
}
