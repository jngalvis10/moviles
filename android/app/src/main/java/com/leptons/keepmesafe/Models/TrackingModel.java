package com.leptons.keepmesafe.Models;

public class TrackingModel {
    private String email, uid, lat, lng;

    public TrackingModel(String email, String uid, String lat, String lng)
    {
        this.email = email;
        this.uid = uid;
        this.lat = lat;
        this.lng = lng;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String Email)
    {
        this.email = Email;
    }

    public String getUID()
    {
        return uid;
    }

    public void setUID(String Uid)
    {
        this.uid = Uid;
    }

    public String getLatitude()
    {
        return lat;
    }

    public void setLatitude(String Latitude)
    {
        this.lat = Latitude;
    }

    public String getLongitude()
    {
        return lng;
    }

    public void setLongitude(String Longitude)
    {
        this.lng = Longitude;
    }
}
