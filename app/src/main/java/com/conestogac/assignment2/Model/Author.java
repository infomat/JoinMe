package com.conestogac.assignment2.Model;

/**
 * This class is to manage user information
 * @param : email, password, fullname, userid
 */
public class Author {
    private String fullname;
    private String uid;
    private String email;

    public Author() {
    }

    public Author(String fullname, String uid, String email) {
        this.fullname = fullname;
        this.uid = uid;
        this.email = email;
    }

    public Author(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
