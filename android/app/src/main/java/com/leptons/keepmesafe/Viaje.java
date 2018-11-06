package com.leptons.keepmesafe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.TextFilterable;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leptons.keepmesafe.Models.Contacto;
import com.leptons.keepmesafe.Models.Punto;
import com.leptons.keepmesafe.Models.Usuario;
import com.leptons.keepmesafe.Models.ViajeM;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Viaje extends AppCompatActivity implements LocationListener, GoogleApiClient
        .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  SensorEventListener {

    SharedPreferences myPreferences;
    private FirebaseAuth mAuth;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private Location previousLocation;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Viaje mShakeDetector;
    public Usuario usuario;
    public Contacto contacto;
    public String id;
    public ViajeM viajeMe;
    public String telefonoE;
    public int metros;
    public double lastlat;
    public double lastlong;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        public void onShake(int count);
    }

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viaje);

        mAuth = FirebaseAuth.getInstance();

        ValueEventListener usuarioListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(dataSnapshot.hasChild(idUser)){
                    usuario = dataSnapshot.child(idUser).getValue(Usuario.class);
                    contacto = usuario.getContactos().get(usuario.getContactos().size()-1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PERFIL", "loadPost:onCancelled", databaseError.toException());
            }
        };
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(usuarioListener);


        if(getIntent().hasExtra("destino")){
            viajeMe = new ViajeM();
            viajeMe.setDestino(getIntent().getExtras().getString("destino"));
            viajeMe.setLatitud(Double.parseDouble(getIntent().getExtras().getString("lat")));
            viajeMe.setLongitud(Double.parseDouble(getIntent().getExtras().getString("long")));

            viajeMe.setContacto(contacto);

            EditText text = findViewById(R.id.destino);
            text.setText(viajeMe.getDestino());
        }

        myPreferences= getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        telefonoE = myPreferences.getString("telefono","");
        metros = 50 + myPreferences.getInt("metros",0);
        myPreferences.getInt("tiempo",0);



        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if(bottomNavigationView!=null){
            Menu menu = bottomNavigationView.getMenu();
            MenuItem menuItem = menu.getItem(1);
            menuItem.setChecked(true);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mapa:
                                Intent intent = new Intent(Viaje.this, Mapa.class);
                                startActivity(intent);
                                break;
                            case R.id.viaje:
                                break;
                            case R.id.tracking:
                                Intent intent2 = new Intent(Viaje.this, Tracking.class);
                                startActivity(intent2);
                                break;
                            case R.id.perfil:
                                Intent intent3 = new Intent(Viaje.this, PerfilFingerprintsAuth.class);
                                startActivity(intent3);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new Viaje();
        mShakeDetector.setOnShakeListener(new OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });
    }

    public void handleShakeEvent(int shakeCounts)
    {
        if(shakeCounts > 1)
        {
            Log.e("ERROR ARCIIIIIIIIIIIIIII", "handleShakeEvent: ");
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                    .findViewById(android.R.id.content)).getChildAt(0);
            emitirAlerta(viewGroup);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            float gForce = (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                mListener.onShake(mShakeCount);
            }
        }
    }

    public void iniciarRecorrido (View view){


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String format = simpleDateFormat.format(Calendar.getInstance().getTime());

        ArrayList<Punto> dp = new ArrayList<Punto>();
        Punto p = new Punto(0,0,Calendar.getInstance().getTime());
        dp.add(p);
        viajeMe.setPuntos(dp);

        id = format + ""+ mAuth.getCurrentUser().getUid();

        viajeMe.setHoraSalida(Calendar.getInstance().getTime());

        FirebaseDatabase.getInstance().getReference().child("viajes").child(id).setValue(viajeMe);

        Button boton = (Button) findViewById(R.id.panico);
        boton.setVisibility(view.VISIBLE);
        Snackbar mySnackbar = Snackbar.make(view , "recorrido iniciado", 1200);
        mySnackbar.show();
        requestSmsPermission();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage( telefonoE ,null, " KMS ALERTA: Se presenta anomalía en el recorrido del usuario, por favor contacte a las autoridades." , null, null);

        } else {
            Toast.makeText(this, "Please enable SMS permission", Toast.LENGTH_SHORT).show();
            requestSmsPermission();

        }




        /*while (enRecorrido){
            if (latitud + longitud>metros){
                //Llamando "alo polisia"
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage( telefonoE ,null, " KMS ALERTA: Se presenta anomalía en el recorrido." , null, null);
            }
        }
        */
    }
    private void requestSmsPermission() {
        String permission = Manifest.permission.SEND_SMS;
        int permissionCheck = ContextCompat.checkSelfPermission(Viaje.this,
                Manifest.permission.SEND_SMS);
        if (ContextCompat.checkSelfPermission(Viaje.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Viaje.this,
                    Manifest.permission.SEND_SMS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(Viaje.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    public void emitirAlerta (View view){
        requestSmsPermission();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager sms = SmsManager.getDefault();
            

                        sms.sendTextMessage( telefonoE ,null, " KMS ALERTA: Me encuentro en emergencia, por favor ayuda. ubicacion" + lastlat + ""+ lastlong , null, null);


                        sms.sendTextMessage( "3163355194" ,null, " KMS ALERTA: El usuario ha activado el botón de pánico. ubicacion" + lastlat + ""+ lastlong, null, null);


            Snackbar mySnackbar = Snackbar.make(view , "Se ha enviado la señal de alerta" +
                    "", 1200);
            mySnackbar.show();

        } else {
            Toast.makeText(this, "Please enable SMS permission", Toast.LENGTH_SHORT).show();
            requestSmsPermission();

        }
    }

    public void buscarDestino (View view){
        Intent intent = new Intent(Viaje.this, BuscarDestino.class);
        startActivity(intent);
        return;
    }


    @Override
    public void onLocationChanged(Location location) {

        Punto p = new Punto(location.getLatitude(), location.getLongitude(),Calendar.getInstance().getTime());
        lastlat = location.getLatitude();
        lastlong = location.getLongitude();

        Log.w("aaaa", lastlat+"");
        if(viajeMe.getPuntos().isEmpty()) {

            ArrayList<Punto> puntos = new ArrayList<>();
            puntos.add(p);
            viajeMe.setPuntos(puntos);
        }
        else {
            viajeMe.getPuntos().add(p);
        }

        FirebaseDatabase.getInstance().getReference().child("viajes").child(id).setValue(viajeMe);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}