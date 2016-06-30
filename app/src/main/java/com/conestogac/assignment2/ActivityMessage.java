package com.conestogac.assignment2;

import android.location.Location;

/**
 * Created by infomat on 16-06-21.
 */
public class ActivityMessage {
    public final int NONE = 0;
    public final int LIKED = 1;
    public final int NOTLIKED = 2;

    private String id;          //message ID
    private String photoUrl;    //photo
    private String name;        //user name
    private String text;//description of activity
    private Location location;  //location
    private Integer like;

    public ActivityMessage() {
    }

    public ActivityMessage(String text, String name, String photoUrl, Location location, Integer like) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.location = location;
        this.like = like;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

}