package com.example.traffichazardsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class SignUp extends AppCompatActivity {

    Button signUp;
    EditText userInput;
    EditText passInput;
    Button back;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        signUp = findViewById(R.id.signUp);
        back = findViewById(R.id.back);
        userInput = findViewById(R.id.userInput1);
        passInput = findViewById(R.id.passInput1);

        Intent Back = new Intent(this,LoginActivity.class);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBack();
            }
        });


        Intent toMain = new Intent(this, MapsActivity.class);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = userInput.getText().toString();
                String password = passInput.getText().toString();
                if (username.equals("")) {
                    Log.i("Username", "Username Required");
                } else {
                    if (password.equals("")) {
                        Log.i("Password", "Password Required");
                    } else {
                        createAccount(username, password);
                    }
                }
            }
        });
    }

    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            toMaps();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });

    }


    public void toMaps()
    {
        Intent intent =  new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    public void toBack(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}