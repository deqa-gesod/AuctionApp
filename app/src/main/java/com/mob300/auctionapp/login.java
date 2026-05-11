package com.mob300.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

// Other necessary imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class login extends AppCompatActivity {
    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mCreateBtn;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    private String userID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.Email1);
        mPassword = findViewById(R.id.Password);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.loginBtn);
        mCreateBtn = findViewById(R.id.createText);
        userID = getIntent().getStringExtra("users");

        // Check if the user is already logged in
        if (fAuth.getCurrentUser() != null) {
            String storedUserRole = getStoredUserRoleFromSharedPreferences();
            if (storedUserRole != null) {
                redirectToAppropriateSection(storedUserRole);
                return; // Exit the method to prevent further execution
            }
        }
    }

    private String getStoredUserRoleFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("userRole", null);
    }

    private void redirectToAppropriateSection(String userRole) {
        if ("admin".equals(userRole)) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            showAlertDialog("Success", "Logged in as Admin");
        } else {
            startActivity(new Intent(getApplicationContext(), BuyProductsActivityUser.class));
            showAlertDialog("Success", "Logged in as User");
        }
        finish(); // Finish the login activity to prevent going back
    }

    public void onClickRegisterFromLogin(View view) {
        startActivity(new Intent(getApplicationContext(), register.class));
    }

    public void onClickLogin(View view) {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showAlertDialog("Error", "Email is Required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showAlertDialog("Error", "Password is Required.");
            return;
        }
        if (password.length() < 6) {
            showAlertDialog("Error", "Password must be >= 6 characters.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Authenticate the user
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    checkUserRoleAndRedirect(fAuth.getCurrentUser().getUid());
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                    handleLoginError(errorMessage);
                }
            }
        });
    }

    private void handleLoginError(String errorMessage) {
        if (errorMessage.contains("INVALID_LOGIN_CREDENTIALS")) {
            showAlertDialog("Login Failed", "Invalid credentials. Please check your email and password.");
        } else if (errorMessage.contains("user may have been deleted")) {
            showAlertDialog("Login Failed", "Account not found. Please register or try a different email.");
        } else if (errorMessage.contains("network error")) {
            showAlertDialog("Network Error", "Please check your internet connection.");
        } else {
            showAlertDialog("Error", errorMessage);
        }

        // Make the forgot password option visible
        TextView textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewForgotPassword.setVisibility(View.VISIBLE);
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPasswordDialog();
            }
        });
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(login.this, R.style.CustomDialogTheme2);
        builder.setTitle("Reset Password");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_reset_password, null);
        final EditText input = dialogView.findViewById(R.id.editTextEmail);

        input.setBackground(ContextCompat.getDrawable(login.this, R.drawable.custom_edittext));
        builder.setView(dialogView);

        builder.setPositiveButton("Send", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> {});

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(ContextCompat.getColor(login.this, R.color.white));
            negativeButton.setTextColor(ContextCompat.getColor(login.this, R.color.white));

            positiveButton.setOnClickListener(view -> {
                String email = input.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && email.matches(".+@gmail\\.com")) {
                    fAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showAlertDialog("Success", "Reset link sent to your email.");
                            dialog.dismiss();
                        } else {
                            showAlertDialog("Error", "Failed to send reset email.");
                        }
                    });
                } else {
                    showAlertDialog("Invalid Email", "Please enter a valid email address.");
                }
            });
        });

        dialog.show();
    }

    private void checkUserRoleAndRedirect(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = dataSnapshot.child("role").getValue(String.class);
                    saveUserRoleInSharedPreferences(userRole);

                    if ("admin".equals(userRole)) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        showAlertDialog("Success", "Logged in as Admin");
                    } else {
                        startActivity(new Intent(getApplicationContext(), BuyProductsActivityUser.class));
                        showAlertDialog("Success", "Logged in as User");
                    }
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showAlertDialog("Error", "Failed to load user data: " + databaseError.getMessage());
            }
        });
    }

    private void saveUserRoleInSharedPreferences(String userRole) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userRole", userRole);
        editor.apply();
    }
}
