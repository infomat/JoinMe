package com.conestogac.assignment2;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        EasyPermissions.PermissionCallbacks {
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvDescription;
        public ImageView imageView;

        public MessageViewHolder(View v) {
            super(v);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            imageView = (ImageView) itemView.findViewById(R.id.ivPhoto);
        }
    }
    //Reference to Database to read
    private Firebase myFirebaseRef;
    private FirebaseAuth mAuth;
    private static final String TAG = MainActivity.class.getSimpleName();

    //Set for GPS
    private static final int REQUEST_ACCESS_FINE_LOCATION = 103;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 104;
    LocationManager myLocationManager;
    String locationProvider = LocationManager.GPS_PROVIDER;
    Location currentLocation = new Location(locationProvider);
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up context to use Firebase
        Firebase.setAndroidContext(this);
        //Get firebase connect
        myFirebaseRef = new Firebase("https://joinme-9831e.firebaseio.com/");

        //Set add button Click listener
        ImageButton btWrite = (ImageButton) findViewById(R.id.btWrite);

        //Todo Make sure location has correct information
        btWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoTakePhoto = new Intent(MainActivity.this, NewPostActivity.class);
                gotoTakePhoto.putExtra(NewPostActivity.LOCATION_EXTRA_NAME, currentLocation);
                startActivity(gotoTakePhoto);
            }
        });

        //Set up GPS service
        myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        setupLocationService();
    }

    /*
        In case of filure in connection
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onPause() {
// TODO Auto-generated method stub
        super.onPause();
        // Remove the listener you previously added
        myLocationManager.removeUpdates(myLocationListener);
    }

    @Override
    protected void onResume() {
// TODO Auto-generated method stub
        super.onResume();
        //Check GPS or Network is enabled
        isGPSEnabled = myLocationManager.isProviderEnabled(myLocationManager.GPS_PROVIDER);
        isNetworkEnabled = myLocationManager.isProviderEnabled(myLocationManager.NETWORK_PROVIDER);

        //If both are unavailable, show error
        if(!isGPSEnabled && !isNetworkEnabled) {
            Context context = getApplicationContext();
            CharSequence text = "Please enable location service at setting menu!";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        } else {
            //first try to get GPS provider then Check Network Provider, because GPS provider has higher accuracy
            if (isGPSEnabled) {
                Log.d(TAG, "Use NETWORK_PROVIDER");
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else {
                Log.d(TAG, "Use GPS_PROVIDER");
                locationProvider = LocationManager.GPS_PROVIDER;
            }
            //start with last known location
            currentLocation = this.getLastKnownLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            // Initialize authentication and set up callbacks
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            //to prevent using back key, remove all task from the stack
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
        Check Fine Location Permission
     */
    @AfterPermissionGranted(REQUEST_ACCESS_FINE_LOCATION)
    private void setupLocationService() {
        //Check
        if (!EasyPermissions.hasPermissions(this, ACCESS_COARSE_LOCATION)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.permission_rationale_gps),
                    REQUEST_ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION);
            return;
        }
    }

    //Get last Known location to get location faster
    private Location getLastKnownLocation() {
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = myLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {

            Location l = myLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private LocationListener myLocationListener
            = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isBetterLocation(location,currentLocation)){
                currentLocation = location;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG,"onProviderDisabled");
            currentLocation = getLastKnownLocation();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG,"onProviderEnabled");
            currentLocation = getLastKnownLocation();
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG,"onStatusChanged");
            currentLocation = getLastKnownLocation();
        }
    };

    //Maintaining a current best estimate
    //Check if the location retrieved is significantly newer than the previous estimate.
    //Check if the accuracy claimed by the location is better or worse than the previous estimate.
    //Check which provider the new location is from and determine if you trust it more.
    private static final int TWO_MINUTES = 1000 * 60 * 10;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;

            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
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
