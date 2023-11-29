package com.example.evchargingstationlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.evchargingstationlocator.Users;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextPhone, editTextName, editTextUsername;
    Button buttonRegister;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    FirebaseDatabase db;
    DatabaseReference reference;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), FeatureSelect.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextUsername = findViewById(R.id.userName);
        editTextName = findViewById(R.id.fullName);
        editTextPhone = findViewById(R.id.phoneNumber);
        buttonRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, Name, Phone, Username;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                Username = String.valueOf(editTextUsername.getText());
                Name = String.valueOf(editTextName.getText());
                Phone = String.valueOf(editTextPhone.getText());


                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(RegisterActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(RegisterActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(Username))
                {
                    Toast.makeText(RegisterActivity.this, "Enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(Name))
                {
                    Toast.makeText(RegisterActivity.this, "Enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(Phone))
                {
                    Toast.makeText(RegisterActivity.this, "Enter your phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                Users users = new Users(Name, Phone, Username, email);
                db = FirebaseDatabase.getInstance();
                reference = db.getReference("Users");
                String uID = reference.push().getKey();
                assert uID != null;
                reference.child(uID).setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            editTextName.setText("");
                            editTextPhone.setText("");
                            editTextUsername.setText("");
                            editTextEmail.setText("");
                            editTextPassword.setText("");

                            Toast.makeText(RegisterActivity.this, "User information added", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this, "Info could not be added", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    Toast.makeText(RegisterActivity.this, "Account created",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegisterActivity.this, "Authentication failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}