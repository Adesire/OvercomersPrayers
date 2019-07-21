package com.overcomersprayer.app.overcomersprayers.models;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class Users {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String deviceToken;

    public Users() {
    }

    public static String getTableName() {
        return "users";
    }

    public Users(String id, String firstName, String lastName, String email, String phone) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.phone = phone;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}

