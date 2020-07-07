package com.example.userlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TRACKING_LOCATION_KEY = "Tracking state :";
    private TextView mLocationTextView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean mTrackingLocation = false;
    private Button mLocationButton;
    private LocationCallback mLocationCallback;
    private Button mButtonWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final Context context = this;
        setContentView(R.layout.activity_main);
        mLocationTextView = findViewById(R.id.locationResults);
        mLocationButton = findViewById(R.id.getLocationButton);
        mButtonWeb = findViewById(R.id.buttonWeb);
        mButtonWeb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ActivityWeb.class);
                startActivity(intent);
            }
        });
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

       /* ImageView mAndroidImageView = findViewById(R.id.imageview_android);
       mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator
       (this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);*/

        mLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!mTrackingLocation) {

                    avviaTracciamento();

                } else {

                    bloccaTracciamento();
                }
            }
        });

        if (savedInstanceState != null) {

            mTrackingLocation = savedInstanceState.getBoolean(
                    TRACKING_LOCATION_KEY);
        }

        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                // If tracking is turned on, reverse geocode into an address
                if (mTrackingLocation) {

                    new FetchAddressTask(MainActivity.this, MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void avviaTracciamento() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

            Log.i("permessi", "Permessi già ottenuti");

        } else {

            mFusedLocationClient.requestLocationUpdates
                    (richiestePosizione(), mLocationCallback,
                            null /* Looper */);
        }

        mLocationTextView.setText(getString(R.string.address_text,
                getString(R.string.loading),
                System.currentTimeMillis()));

        // mRotateAnim.start();
        mTrackingLocation = true;
        mLocationButton.setText(R.string.stop_tracking);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient.getLastLocation().addOnSuccessListener(
                            new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {

                                    Log.i("onSuccess", "onSuccess eseguito");

                                    if (location != null) {

                                        // Start the reverse geocode AsyncTask
                                        new FetchAddressTask(MainActivity.this,
                                                MainActivity.this).execute(location);
                                    } else {

                                        mLocationTextView.setText(R.string.loc_error);
                                    }
                                }
                            });
                }
                    else {

                    Toast.makeText(this,
                            "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                    break;
        }
    }

    @Override
    public void onTaskCompleted(String result) {

        // Update the UI
        mLocationTextView.setText(getString(R.string.address_text,
                result, System.currentTimeMillis()));
    }

    private void bloccaTracciamento() {

        if (mTrackingLocation) {

            mTrackingLocation = false;
            mLocationButton.setText(R.string.get_location);
            mLocationTextView.setText(R.string.location_text);
           // mRotateAnim.end();
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        }
    }

    private LocationRequest richiestePosizione() {

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (mTrackingLocation) {

            avviaTracciamento();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (mTrackingLocation) {

            bloccaTracciamento();
            mTrackingLocation = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

}
