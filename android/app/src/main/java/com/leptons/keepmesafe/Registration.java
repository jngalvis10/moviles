package com.leptons.keepmesafe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.leptons.keepmesafe.Models.Contacto;
import com.leptons.keepmesafe.Models.Usuario;

import java.util.ArrayList;

public class Registration extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private EditText mName, mPhone, mEmail, mPassword, mCPassword;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null)
                {
                    Intent intent = new Intent(Registration.this, Viaje.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mName = findViewById(R.id.fname);
        mEmail = findViewById(R.id.user);
        mPhone = findViewById(R.id.phone);
        mPassword = findViewById(R.id.pass);
        mCPassword = findViewById(R.id.confirmpass);
        mRegister = findViewById(R.id.register);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String phone = mPhone.getText().toString();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String cpassword = mCPassword.getText().toString();
                final String name = mName.getText().toString();



                if(phone.isEmpty()||email.isEmpty()||password.isEmpty()||name.isEmpty())
                {
                    Toast.makeText(Registration.this, " Missing Fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if(!password.equals(cpassword))
                {
                    Toast.makeText(Registration.this, "Passwords do not correspond", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful())
                        {
                            Toast.makeText(Registration.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            String userId = mAuth.getCurrentUser().getUid();
                            ArrayList<Contacto> lista = new ArrayList<Contacto>();
                            lista.add(new Contacto("123","123@123.com",123l));
                            Usuario user = new Usuario(userId,name,"Status" ,email,Long.parseLong(phone), 10, 10,lista);
                            FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(user);
                        }
                    }
                });

            }
        });
    }

    public void backToLogin(View view)
    {
        Intent intent = new Intent(Registration.this, Login.class);
        startActivity(intent);
        finish();
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}
