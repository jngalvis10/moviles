package com.leptons.keepmesafe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Login extends AppCompatActivity {
   private FirebaseAuth mAuth;
   private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private LinearLayout loginLayout;
    private Button mLogin, mRegister;
    private EditText mUser, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
           @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null) {
                   Intent intent = new Intent(Login.this, Viaje.class);
                   startActivity(intent);
                   finish();
                   return;
                }
            }
        };

        mLogin = findViewById(R.id.login);
        mRegister = findViewById(R.id.register);
        mUser = findViewById(R.id.user);
        mPassword = findViewById(R.id.pass);
        loginLayout = findViewById(R.id.activity_login);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = mUser.getText().toString();
                final String password = mPassword.getText().toString();

                if(username.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(Login.this, "Missing Field", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                   @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      if(!task.isSuccessful())
                        {
                           Toast.makeText(Login.this, "Sign In Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    public void toResetPassword(View view) {
        Intent intent = new Intent(Login.this, ResetPassword.class);
        startActivity(intent);
        finish();
        return;
    }
}
