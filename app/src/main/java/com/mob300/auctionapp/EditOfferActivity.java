package com.mob300.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.util.Log;


public class EditOfferActivity extends AppCompatActivity {

    private EditText mNameEditText, mDescriptionEditText, mPhoneNumberEditText;
    private Button mSaveButton;
    private Offer mOffer;
    private DatabaseReference mOffersDatabaseRef;
    private DatabaseReference mProductsDatabaseRef;
    private DatabaseReference mServicesDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_offer);
        mProductsDatabaseRef = FirebaseDatabase.getInstance().getReference("products");
        mServicesDatabaseRef = FirebaseDatabase.getInstance().getReference("services");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        mOffer = (Offer) intent.getSerializableExtra("offer");

        mNameEditText = findViewById(R.id.offer_name_edit_text);
        mDescriptionEditText = findViewById(R.id.offer_description_edit_text);
        mPhoneNumberEditText = findViewById(R.id.offer_phone_number_edit_text);
        mSaveButton = findViewById(R.id.save_button);

        if (mOffer != null) {
            mNameEditText.setText(mOffer.getName());
        }

        mOffersDatabaseRef = FirebaseDatabase.getInstance().getReference("offers");

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOffer();
            }
        });

        Log.d("EditOfferActivity", "Received Offer: " + mOffer);

    }

    // Handle the back button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateOffer() {
        String name = mNameEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();
        String phoneNumber = mPhoneNumberEditText.getText().toString().trim();


        if (!name.isEmpty() && !description.isEmpty() && !phoneNumber.isEmpty() && mOffer != null && mOffer.getId() != null) {
            mOffer.setName(name);


            DatabaseReference targetRef;
            String nameField, descriptionField, phoneNumberField;

            if (mOffer instanceof Product) {
                targetRef = mProductsDatabaseRef;
                nameField = "productName";
                descriptionField = "productDescription";
                phoneNumberField = "contactInfo";
            } else if (mOffer instanceof Service) {
                targetRef = mServicesDatabaseRef;
                nameField = "serviceName";
                descriptionField = "serviceDescription";
                phoneNumberField = "contactInfo";

            } else {
                Toast.makeText(this, "Invalid offer type", Toast.LENGTH_SHORT).show();
                return;
            }

            targetRef.child(mOffer.getId()).child(nameField).setValue(name); // Update the name field
            targetRef.child(mOffer.getId()).child(descriptionField).setValue(description); // Update the description field
            targetRef.child(mOffer.getId()).child(phoneNumberField).setValue(phoneNumber) // Update the description field

                    // If you have other fields to update, add them here as well

                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditOfferActivity.this, "Offer updated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditOfferActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditOfferActivity.this, "Error updating offer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
