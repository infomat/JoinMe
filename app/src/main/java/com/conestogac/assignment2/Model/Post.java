package com.conestogac.assignment2.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by infomat on 16-06-26.
 */
public class Post {
    private Author author;
    private String full_url;
    private String thumb_storage_uri;
    private String thumb_url;
    private String text;
    private Object timestamp;
    private String full_storage_uri;

    public Post() {
        // empty default constructor, necessary for Firebase to be able to deserialize blog posts
    }

    public Post(Author author, String full_url, String full_storage_uri, String thumb_url, String thumb_storage_uri, String text, Object timestamp) {
        this.author = author;
        this.full_url = full_url;
        this.text = text;
        this.timestamp = timestamp;
        this.thumb_storage_uri = thumb_storage_uri;
        this.thumb_url = thumb_url;
        this.full_storage_uri = full_storage_uri;
    }

    public Author getAuthor() {
        return author;
    }

    public String getFull_url() {
        return full_url;
    }

    public String getText() {
        return text;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public String getThumb_storage_uri() {
        return thumb_storage_uri;
    }

    //com.fasterxml.jackson.core:jackson-databind:2.7.3 JSON
    //thumb_url is used as json's property i.e. {thumb_url: "xxxxx"}
    @JsonProperty("thumb_url")
    public String getThumb_url() {
        return thumb_url;
    }

    public String getFull_storage_uri() {
        return full_storage_uri;
    }
}