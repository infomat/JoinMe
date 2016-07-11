package com.conestogac.assignment2;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class ListPostActivity extends AppCompatActivity implements
        PostFragment.OnPostSelectedListener{

    //Reference to Database to read
    private FirebaseAuth mAuth;
    private static final String TAG = ListPostActivity.class.getSimpleName();

    //Set for GPS
    LocationManager myLocationManager;
    static String locationProvider = LocationManager.GPS_PROVIDER;
    static Location currentLocation = new Location(locationProvider);
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    //Will be used to filter out with distanceSetting
    public long distanceSetting = 0;

    //Widgets
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_post);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        distanceSetting = sharedPref.getInt(getString(R.string.saved_distance), 0);

        //Set Title
        setTitle("Distance: "+ String.valueOf(distanceSetting) + " Kms");

        //Set up GPS service
        myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        currentLocation = getLastKnownLocation();

        //Set add button Click listener
        //Todo Make sure location has correct information
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.isAnonymous()) {
                    Toast.makeText(ListPostActivity.this, "You must sign-in to post.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent gotoTakePhoto = new Intent(ListPostActivity.this, NewPostActivity.class);
                gotoTakePhoto.putExtra(NewPostActivity.LOCATION_EXTRA_NAME, currentLocation);
                startActivity(gotoTakePhoto);
            }
        });

        //put location and create new fragment
        Fragment postFragment = PostFragment.newInstance();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Remove the listener you previously added
        myLocationManager.removeUpdates(myLocationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check GPS or Network is enabled
        isGPSEnabled = myLocationManager.isProviderEnabled(myLocationManager.GPS_PROVIDER);
        isNetworkEnabled = myLocationManager.isProviderEnabled(myLocationManager.NETWORK_PROVIDER);

        //update setting distance
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        distanceSetting = sharedPref.getInt(getString(R.string.saved_distance), 0);
        setTitle("Distance: "+ String.valueOf(distanceSetting) + "kms");

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

    /*
        This will be used when icon is selected
        State change None->Like->NotLike->None
                //If data exists, it is like->dislike or notlike->None
                //If data does not exist, it is none -> Like
     */
    @Override
    public void onPostChangeLikeStatus(final String postKey) {
        final String userKey = FirebaseUtil.getCurrentUserId();
        //todo check likes reference which will be under users folder
        final DatabaseReference postLikesRef = FirebaseUtil.getLikesRef();

        Log.d(TAG, "onPostLike() UserKey: "+ userKey);
        postLikesRef.child(postKey).child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()) {
                    if ((long)dataSnapshot.getValue() == 1) {
                        postLikesRef.child(postKey).child(userKey).setValue(2);
                    } else {
                        postLikesRef.child(postKey).child(userKey).removeValue();
                    }
                } else {
                    postLikesRef.child(postKey).child(userKey).setValue(1);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    /*
        If user swipe left, it will set like
     */
    @Override
    public void onPostLike(final String postKey) {
        final String userKey = FirebaseUtil.getCurrentUserId();
        //todo check likes reference which will be under users folder
        final DatabaseReference postLikesRef = FirebaseUtil.getLikesRef();

        Log.d(TAG, "onPostLike() UserKey: "+ userKey);
        postLikesRef.child(postKey).child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if ((long)dataSnapshot.getValue() == 2) {
                        postLikesRef.child(postKey).child(userKey).setValue(1);
                    }
                } else {
                    postLikesRef.child(postKey).child(userKey).setValue(1);
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    /*
        If user swipe left, it will set dislike
    */
    @Override
    public void onPostDisLike(final String postKey) {
        final String userKey = FirebaseUtil.getCurrentUserId();
        //todo check likes reference which will be under users folder
        final DatabaseReference postLikesRef = FirebaseUtil.getLikesRef();

        Log.d(TAG, "onPostLike() UserKey: "+ userKey);
        postLikesRef.child(postKey).child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if ((long)dataSnapshot.getValue() == 1) {
                        postLikesRef.child(postKey).child(userKey).setValue(2);
                    }
                } else {
                    postLikesRef.child(postKey).child(userKey).setValue(2);
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
        Process Actionbar menu event.
        Show Like, DisLike, All will be processed within PostFragment
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_logout:
                // Initialize authentication and set up callbacks
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                //to prevent using back key, remove all task from the stack
                Intent intent = new Intent(ListPostActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Get last Known location to get location faster
    private Location getLastKnownLocation() {
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = myLocationManager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers) {
            //Permission was approved at the beginning
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

    /*
        Location Listener which is caleed Provider's status changed
     */
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
     * location  The new Location that you want to evaluate
     * currentBestLocation  The current Location fix, to which you want to compare the new one
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
}
