package com.conestogac.assignment2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = WelcomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_up_button).setOnClickListener(this);

        //todo load customer infomation saved

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

}
