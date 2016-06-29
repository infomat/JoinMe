package com.conestogac.assignment2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    //Instances of Firebase
    private FirebaseAuth mAuth;
    private String fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_up_button).setOnClickListener(this);

        //todo load customer infomation saved
        //Set up for firebase Auth listener
        //Get shared instance of the firebaseAuth object
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            GetCurrentUserFullname();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sign_up_button:
                Intent signUpIntent = new Intent(this, LoginActivity.class);
                signUpIntent.putExtra(LoginActivity.PROFILE_MODE_EXTRA_NAME,LoginActivity.MODE_SIGNUP);
                startActivity(signUpIntent);
                break;
            case R.id.sign_in_button:
                Intent signInIntent = new Intent(this, LoginActivity.class);
                signInIntent.putExtra(LoginActivity.PROFILE_MODE_EXTRA_NAME,LoginActivity.MODE_SIGNIN);
                startActivity(signInIntent);
                break;
        }
    }

    /*
        Get Current user's full name
        At the callback (after getting name), it will transition to listview
     */
    public void GetCurrentUserFullname() {
        if (FirebaseUtil.getCurrentUserId() != null) {
            FirebaseUtil.getBaseRef().child("users").child(FirebaseUtil.getCurrentUserId()).child("fullname").
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                fullName = (String) dataSnapshot.getValue();
                                Toast.makeText(WelcomeActivity.this, "Welcome back! "+ fullName,
                                        Toast.LENGTH_SHORT).show();
                                //To prevent user back to this activity by backkey
                                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
        fullName = "Anonymous";
        Log.e(TAG, "something wrong");
    }
}
