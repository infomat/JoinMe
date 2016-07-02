package com.conestogac.assignment2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class WelcomeActivity extends AppCompatActivity implements
        View.OnClickListener,
        EasyPermissions.PermissionCallbacks {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    //Instances of Firebase
    private FirebaseAuth mAuth;
    private String fullName;

    //For GPS permission
    private static final int REQUEST_ACCESS_FINE_LOCATION = 100;

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

    /*
        Process Onclick for Signup, Signin
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (!checkPermission()) return;
        switch (id) {
            case R.id.sign_up_button:
                //to avoid problem of getting GPS at FeedsActvity, get grant here
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
        if (FirebaseUtil.getCurrentUserId() != null && checkPermission()) {
            FirebaseUtil.getBaseRef().child("users").child(FirebaseUtil.getCurrentUserId()).child("fullname").
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                fullName = (String) dataSnapshot.getValue();
                                Toast.makeText(WelcomeActivity.this, "Welcome back! "+ fullName,
                                        Toast.LENGTH_SHORT).show();
                                FirebaseUtil.setFullName(fullName);
                                //To prevent user back to this activity by backkey
                                Intent intent = new Intent(WelcomeActivity.this, FeedsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    /*
    Check Fine Location Permission
    */
    @AfterPermissionGranted(REQUEST_ACCESS_FINE_LOCATION)
    private boolean checkPermission() {
        //Check
        if (!EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.permission_rationale_gps),
                    REQUEST_ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION);
            return false;
        }
        return true;
    }

    /*
      Callback received when a permissions request has been completed.
   */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }
}
