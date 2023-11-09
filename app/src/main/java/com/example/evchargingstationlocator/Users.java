package com.example.evchargingstationlocator;

public class Users {

    String fullName, phoneNumber, userName;

    public Users() {
    }

    public Users(String fullName, String phoneNumber, String userName) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
