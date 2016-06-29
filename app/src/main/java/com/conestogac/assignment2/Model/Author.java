package com.conestogac.assignment2.Model;

/**
 * Created by infomat on 16-06-26.
 */
public class Author {
    private String full_name;
    private String profile_picture;

    private String uid;

    public Author() {
    }

    public Author(String full_name, String uid) {
        this.full_name = full_name;
        this.uid = uid;
    }

    public Author(String full_name, String profile_picture, String uid) {
        this.full_name = full_name;
        this.profile_picture = profile_picture;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getProfile_picture() {
        return profile_picture;
    }
}
