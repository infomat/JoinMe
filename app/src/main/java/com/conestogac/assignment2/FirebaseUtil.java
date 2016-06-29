package com.conestogac.assignment2;

import android.util.Log;

import com.conestogac.assignment2.Model.Author;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Util class for Firebase which will return paths
 */
public class FirebaseUtil {

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

    public static Author getAuthor() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return null;

        return new Author(user.getEmail(), user.getUid());
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

}
