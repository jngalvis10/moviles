package com.leptons.keepmesafe;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.leptons.keepmesafe.Models.TrackingModel;

import java.text.DecimalFormat;

public class TrackingMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String email;
    DatabaseReference locations;

    Double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Ref to Firebase
        locations = FirebaseDatabase.getInstance().getReference("Locations");

        //Get Intent
        if(getIntent() != null)
        {
            email = getIntent().getStringExtra("email");
            lat = getIntent().getDoubleExtra("lat",0);
            lng = getIntent().getDoubleExtra("lng",0);

        }

        if(!TextUtils.isEmpty(email))
            loadLocationForThisUser(email);
    }


    private void loadLocationForThisUser(String email)
    {
        Query user_location = locations.orderByChild("email").equalTo(email);
        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot: dataSnapshot.getChildren())
                {
                    TrackingModel tracking = postSnapShot.getValue(TrackingModel.class);

                    //Addd marker for associet location
                    LatLng familyLocation = new LatLng(Double.parseDouble(tracking.getLatitude()),
                                                        Double.parseDouble(tracking.getLongitude()));

                    //Create location from user coordinates
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    //Create location from friend coordintes
                    Location familyMember = new Location("");
                    familyMember.setLatitude(Double.parseDouble(tracking.getLatitude()));
                    familyMember.setLongitude(Double.parseDouble(tracking.getLongitude()));


                    //Claer Old Markers
                    mMap.clear();

                    //Add Family Marker
                    mMap.addMarker(new MarkerOptions()
                                    .position(familyLocation)
                                    .title(tracking.getEmail())
                                    .snippet("Distance " + new DecimalFormat("#.#").format((currentUser.distanceTo(familyMember))/1000) + "  Km")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), 12.0f));
                }

                //Add marker current user
                LatLng current = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions()
                                .position(current)
                                .title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private double distance(Location currentUser, Location familyMember)
    {
        double theta = currentUser.getLongitude() - familyMember.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude()))
                    * Math.sin(deg2rad(familyMember.getLatitude()))
                    * Math.cos(deg2rad(currentUser.getLatitude()))
                    * Math.cos(deg2rad(familyMember.getLatitude()))
                    * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);

    }

    private double deg2rad(double deg)
    {
        return (deg * Math.PI/180.0);
    }

    private double rad2deg(double rad)
    {
        return (rad * 180.0/Math.PI);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}
