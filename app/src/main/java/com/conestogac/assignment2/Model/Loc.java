package com.conestogac.assignment2.Model;

/**
 * Created by infomat on 16-06-29.
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
