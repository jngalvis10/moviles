package com.leptons.keepmesafe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leptons.keepmesafe.Models.Contacto;
import com.leptons.keepmesafe.Models.Excepcion;
import com.leptons.keepmesafe.Models.Usuario;

import java.util.ArrayList;

public class Perfil extends AppCompatActivity  {

    public Usuario usuario;
    public Contacto contacto;
    private String id;
    private EditText nombre,correo, numero, metros,tiempo;


    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.contacto);
        numero = findViewById(R.id.telefono);
        metros = findViewById(R.id.metros);
        tiempo = findViewById(R.id.tiempo);

        ValueEventListener usuarioListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(dataSnapshot.hasChild(id)){
                    usuario = dataSnapshot.child(id).getValue(Usuario.class);

                    contacto = usuario.getContactos().get(usuario.getContactos().size()-1);
                    nombre.setText(contacto.username);
                    correo.setText(contacto.email);
                    numero.setText(contacto.phone+"");
                    metros.setText(usuario.distancia+"");
                    tiempo.setText(usuario.tiempoEspera+"");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PERFIL", "loadPost:onCancelled", databaseError.toException());
            }
        };
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(usuarioListener);

        myPreferences= getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        myEditor = myPreferences.edit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mapa:
                                Intent intent = new Intent(Perfil.this, Mapa.class);
                                startActivity(intent);
                                break;
                            case R.id.viaje:
                                Intent intent2 = new Intent(Perfil.this, Viaje.class);
                                startActivity(intent2);
                                break;
                            case R.id.tracking:
                                Intent intent3 = new Intent(Perfil.this, Tracking.class);
                                startActivity(intent3);
                                break;
                            case R.id.perfil:
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
    }

    public void agregarExcepciones (View view){

        ArrayList excepciones= new ArrayList<>();
        int metros = Integer.parseInt( this.metros.getText().toString());
        int tiempo = Integer.parseInt( this.tiempo.getText().toString());

        myEditor.putInt("metros", metros);
        myEditor.putInt("tiempo", tiempo);
        myEditor.commit();

        Excepcion one= new Excepcion(metros, tiempo);
        excepciones.add(one);

          InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        usuario.setDistancia(metros);
        usuario.setTiempoEspera(tiempo);
        FirebaseDatabase.getInstance().getReference().child("users").child(id).setValue(usuario);


        Snackbar mySnackbar = Snackbar.make(view ,  "Excepción añadida", 1200);
        mySnackbar.show();



    }

    public void agregarContacto (View view){

        myEditor.putString("user", correo.getText().toString());
        myEditor.putString("phone",numero.getText().toString());
        myEditor.commit();


        if(!correo.getText().toString().equals(contacto.email)){

            Contacto nuevo = new Contacto(nombre.getText().toString(),correo.getText().toString(),Long.parseLong(numero.getText().toString()));
            usuario.getContactos().add(nuevo);
            contacto = nuevo;

            FirebaseDatabase.getInstance().getReference().child("users").child(id).setValue(usuario);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            Snackbar mySnackbar = Snackbar.make(view , contacto.email + " ha sido añadido", 1200);
            mySnackbar.show();Button boton = (Button) findViewById(R.id.botcnRreferencias);

        } else {
            usuario.getContactos().get(usuario.getContactos().size()-1).setUsername(nombre.getText().toString());
            usuario.getContactos().get(usuario.getContactos().size()-1).setPhone(Long.parseLong(numero.getText().toString()));

            FirebaseDatabase.getInstance().getReference().child("users").child(id).setValue(usuario);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            Snackbar mySnackbar = Snackbar.make(view , contacto.email + " ha sido actualizado", 1200);
            mySnackbar.show();Button boton = (Button) findViewById(R.id.botcnRreferencias);
        }

    }

}
