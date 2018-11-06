package com.leptons.keepmesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leptons.keepmesafe.Models.Contacto;
import com.leptons.keepmesafe.Models.TrackingModel;
import com.leptons.keepmesafe.Models.Usuario;

import java.util.ArrayList;

public class Tracking extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    DatabaseReference onlineRef, currentUserRef, counterRef, locations;
    FirebaseRecyclerAdapter<Usuario,TrackingUserList> adapter;

    //View
    RecyclerView listOnline;
    RecyclerView.LayoutManager layoutManager;

    //location
    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 5000;
    private static int DISTANCE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        //Init View
        listOnline = (RecyclerView)findViewById(R.id.listUsersOnline);
        listOnline.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listOnline.setLayoutManager(layoutManager);

        //Set Toolbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        locations = FirebaseDatabase.getInstance().getReference("Locations");
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline");
        currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,},
                    MY_PERMISSION_REQUEST_CODE );
        }
        else {
            if(checkPlayServices())
            {
                createLocationRequest();
                buildGoogleApiClient();
                displayLocation();
            }
        }

        SetUpSystem();
        UpdateList();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if(bottomNavigationView!=null){
            Menu menu = bottomNavigationView.getMenu();
            MenuItem menuItem = menu.getItem(2);
            menuItem.setChecked(true);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mapa:
                                Intent intent = new Intent(Tracking.this, Mapa.class);
                                startActivity(intent);
                                break;
                            case R.id.viaje:
                                Intent intent2 = new Intent(Tracking.this,Viaje.class);
                                startActivity(intent2);
                                break;
                            case R.id.tracking:
                                break;
                            case R.id.perfil:
                                Intent intent3 = new Intent(Tracking.this, PerfilFingerprintsAuth.class);
                                startActivity(intent3);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

    }

    private void SetUpSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Boolean.class))
                {
                    currentUserRef.onDisconnect().removeValue();
                    counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new Usuario(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                                    "Online",
                                    FirebaseAuth.getInstance().getCurrentUser().getEmail(),34,
                                    10,
                                    3, new ArrayList<Contacto>()));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                    {
                        Usuario usuario = postSnapshot.getValue(Usuario.class);
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void UpdateList()
    {
        FirebaseRecyclerOptions<Usuario> userOptions = new FirebaseRecyclerOptions.Builder<Usuario>()
                .setQuery(counterRef,Usuario.class)
                .build();

       adapter = new FirebaseRecyclerAdapter<Usuario, TrackingUserList>(userOptions) {
           @Override
           protected void onBindViewHolder(@NonNull TrackingUserList viewHolder, int position, @NonNull final Usuario model) {
               viewHolder.userName.setText(model.getEmail());

               viewHolder.itemClickListener = new ItemClickListener() {
                   @Override
                   public void onClick(View view, int position) {
                       // if model is current user not set click event
                       if(!model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                       {
                           Intent map = new Intent(Tracking.this,TrackingMapActivity.class );
                           map.putExtra("email",model.getEmail());
                           map.putExtra("lat", mLastLocation.getLatitude());
                           map.putExtra("lng", mLastLocation.getLongitude());
                           startActivity(map);
                       }
                   }
               };
           }

           @NonNull
           @Override
           public TrackingUserList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View itemview = LayoutInflater.from(getBaseContext())
                       .inflate(R.layout.users_layout,parent , false);
               return new TrackingUserList(itemview);
           }
       };
        adapter.startListening();
        listOnline.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!= ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this, PLAY_SERVICES_RES_REQUEST);
            }
            else
            {
                Toast.makeText(this,"Este dispositivo no es soportado", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private synchronized void buildGoogleApiClient(){

        this.mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

       this. mGoogleApiClient.connect();
    }

    private void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void displayLocation()
    {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= getPackageManager().PERMISSION_GRANTED )
        {
           return ;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLastLocation != null)
        {
            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new TrackingModel(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude())));
        }
        else {

            Toast.makeText(this,"No podemos actulizar la ubicacion ",Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates()
    {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= getPackageManager().PERMISSION_GRANTED )
        {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, mLocationRequest, this);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        this.mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@Nullable ConnectionResult connectionResult)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case(R.id.logOut):
                currentUserRef.removeValue();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(this.mGoogleApiClient != null)
        {
           this.mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop()
    {
        if(this.mGoogleApiClient != null)
        {
            this.mGoogleApiClient.disconnect();
        }

        if(adapter != null)
        {
            adapter.stopListening();
        }
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkPlayServices();
    }

}
