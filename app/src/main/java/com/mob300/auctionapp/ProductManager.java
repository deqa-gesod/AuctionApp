package com.mob300.auctionapp;

import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductManager {

    private FirebaseDatabase database;

    public ProductManager() {
        database = FirebaseDatabase.getInstance();
    }

    /**
     * Moves expired products from "products" node to "sold_products" node.
     */
    public void moveExpiredProducts() {
        DatabaseReference productsRef = database.getReference("products");

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Access the Product_details node to get the deadline
                    DataSnapshot productDetailsSnapshot = productSnapshot.child("Product_details");
                    String deadlineStr = productDetailsSnapshot.child("deadline").getValue(String.class);

                    if (deadlineStr == null) continue; // Skip if deadline is not available

                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date deadline = dateFormat.parse(deadlineStr);
                        Date now = new Date();

                        if (deadline.before(now)) {
                            // Deadline has passed, move product to "sold_products"
                            DatabaseReference soldProductsRef = database.getReference("sold_products");
                            soldProductsRef.child(productSnapshot.getKey()).setValue(productSnapshot.getValue())
                                    .addOnSuccessListener(aVoid -> {
                                        // Remove from "products" after successful copy
                                        productsRef.child(productSnapshot.getKey()).removeValue();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ProductManager", "Failed to move expired product", e);
                                    });
                        }
                    } catch (ParseException e) {
                        Log.e("ProductManager", "Failed to parse deadline: " + deadlineStr, e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ProductManager", "Failed to fetch products", databaseError.toException());
            }
        });
    }


    // Add other product-related methods here as needed
}

