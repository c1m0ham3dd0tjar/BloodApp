package com.example.licenta.model;

import com.facebook.CallbackManager;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class Appointment {

    private int day, month, year, hour, minutes;
    private String donationCenter, patientName, contactNumber, key;
    private boolean donationConfirmed;
    private String reminder;

    public Appointment() {
    }

    public Appointment(int day, int month, int year, int hour, int minutes, String donationCenter, String patientName,
                       String contactNumber, String key, boolean donationConfirmed, String reminder) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minutes = minutes;
        this.donationCenter = donationCenter;
        this.patientName = patientName;
        this.contactNumber = contactNumber;
        this.key = key;
        this.donationConfirmed = donationConfirmed;
        this.reminder = reminder;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getDonationCenter() {
        return donationCenter;
    }

    public void setDonationCenter(String donationCenter) {
        this.donationCenter = donationCenter;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isDonationConfirmed() {
        return donationConfirmed;
    }

    public void setDonationConfirmed(boolean donationConfirmed) {
        this.donationConfirmed = donationConfirmed;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }
}
