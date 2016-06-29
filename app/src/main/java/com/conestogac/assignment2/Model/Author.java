package com.conestogac.assignment2.Model;

/**
 * Created by infomat on 16-06-26.
 */
public class Author {
    private String fullname;
    private String profile_picture;

    private String uid;

    public Author() {
    }

    public Author(String fullname, String uid) {
        this.fullname = fullname;
        this.uid = uid;
    }

    public Author(String fullname, String profile_picture, String uid) {
        this.fullname = fullname;
        this.profile_picture = profile_picture;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfile_picture() {
        return profile_picture;
    }
}
