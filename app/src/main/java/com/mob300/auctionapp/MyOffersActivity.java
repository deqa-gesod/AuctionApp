package com.mob300.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyOffersActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MyOffersAdapter mAdapter;
    private DatabaseReference mProductsDatabaseRef;
    private DatabaseReference mServicesDatabaseRef;
    private List<Product> mOffers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = findViewById(R.id.my_offers_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mOffers = new ArrayList<>();
        mAdapter = new MyOffersAdapter(MyOffersActivity.this, mOffers);
        mRecyclerView.setAdapter(mAdapter);

        // Initialize Firebase
        mProductsDatabaseRef = FirebaseDatabase.getInstance().getReference("products");

        // Fetch products data from Firebase
        mProductsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mOffers.clear();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productId = productSnapshot.getKey();

                    // Continue to retrieve Product details
                    DataSnapshot productDetailsSnapshot = productSnapshot.child("Product_details");
                    DataSnapshot productImagesSnapshot = productSnapshot.child("imageUrls");

                    Product product = null;
                    if (productDetailsSnapshot.exists()) {
                        product = productDetailsSnapshot.getValue(Product.class);
                    }

                    List<String> imageUrls = new ArrayList<>();
                    for (DataSnapshot imageSnapshot : productImagesSnapshot.getChildren()) {
                        String imageUrl = imageSnapshot.getValue(String.class);
                        if (imageUrl != null) {
                            imageUrls.add(imageUrl);
                        }
                    }

                    // Access the "offeredPrices" node
                    DataSnapshot offeredPricesSnapshot = productSnapshot.child("offeredPrices");
                    boolean isUserOwner = false;
                    for (DataSnapshot offeredPrice : offeredPricesSnapshot.getChildren()) {
                        String ownerId = offeredPrice.child("userId").getValue(String.class);
                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(ownerId)) {
                            isUserOwner = true;
                            break; // No need to check further if we found a match
                        }
                    }

                    // Add to list if product is valid and current user is owner
                    if (product != null && isUserOwner) {
                        product.setProductId(productId); // Set the product ID
                        product.setImageUrls(imageUrls); // Set the image URLs
                        mOffers.add(product);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyOffersActivity.this, databaseError.getMessage(),  Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
