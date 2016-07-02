package com.conestogac.assignment2;

import android.location.Location;
import android.util.Log;

import com.conestogac.assignment2.Model.Author;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Util class for Firebase which will return paths
 */
public class FirebaseUtil {
    private static String currentUserFullname;

    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static void setFullName(String username) {
        currentUserFullname = username;
    }

    public static Author getAuthor() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;

        return new Author(currentUserFullname, user.getUid());
    }


    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("users").child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getPostsRef() {
        return getBaseRef().child("posts");
    }

    public static String getPostsPath() {
        return "posts/";
    }

    public static String getUsersPath() {
        return "users/";
    }

    //If user select unscribe specific post, it will store this id
    public static String getUnscribePath() {
        return "unscribe/";
    }

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    public static DatabaseReference getLikesRef() {
        return getBaseRef().child("likes");
    }

}
