package com.conestogac.assignment2.Model;

/**
 * This class is to manage location information of user or uploaded post's GPS location
 * @param : logitude, latitude
 */
public class Loc {
    private String longitude;
    private String latitude;

    public Loc() {
    }

    public Loc(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getlongitude() {
        return longitude;
    }

    public String getlatitude() {
        return latitude;
    }

}
