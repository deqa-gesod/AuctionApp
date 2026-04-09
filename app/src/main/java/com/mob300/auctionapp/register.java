package com.mob300.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map; // Add this import
import java.util.HashMap; // Add this import

public class register extends AppCompatActivity {
    private static final String TAG = register.class.getSimpleName();
    EditText mFullName,mEmail,mPassword,mPhone;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn= findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createText);
        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        database = FirebaseDatabase.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

    }
    public void onClickLoginFromRegister (View view){
        startActivity(new Intent(getApplicationContext(),login.class));
    }

    public void onClickRegister (View view){
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        final String fullName = mFullName.getText().toString();
        final String phone = mPhone.getText().toString();

        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email is Required.");
            return;
        }
        // Email constraint
        if (!email.matches(".+@gmail\\.com")) {
            mEmail.setError("Email must end with '@gmail.com'.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is Required.");
            return;
        }

        // Password constraint
        if (password.length() < 9) {
            mPassword.setError("Password must be > 8 characters.");
            return;
        }


        progressBar.setVisibility(View.VISIBLE);

        // Now, we register the user in Firebase
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Store user information in Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("fullName", fullName);
                    user.put("email", email);
                    user.put("phone", phone);


                    // With this block:
                    DatabaseReference userRef = database.getReference("users").child(fAuth.getCurrentUser().getUid());
                    userRef.child("fullName").setValue(fullName);
                    userRef.child("email").setValue(email);
                    userRef.child("role").setValue("user");
                    userRef.child("phone").setValue(phone)

                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(register.this, "User Created.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), BuyProductsActivityUser.class));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(register.this, "Error ! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    DatabaseReference roleRef = database.getReference("users").child(fAuth.getCurrentUser().getUid()).child("role");
                    roleRef.setValue("user").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Redirect to appropriate activity after registration
                            redirectToActivityBasedOnRole();
                        }
                    });



                } else {
                    Toast.makeText(register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void redirectToActivityBasedOnRole() {
        // Retrieve user role from the database
        DatabaseReference roleRef = database.getReference("users").child(fAuth.getCurrentUser().getUid()).child("role");
        roleRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    String role = task.getResult().getValue(String.class);
                    if (role != null) {
                        if (role.equals("admin")) {
                            // Redirect to MainActivity for admins
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else if (role.equals("user")) {
                            // Redirect to BuyProductsActivityUser for users
                            startActivity(new Intent(getApplicationContext(), BuyProductsActivityUser.class));
                        }
                        // Finish the current activity to prevent going back to registration
                        finish();
                    }
                } else {
                    // Handle the case where role retrieval fails
                }
            }
        });
    }
}