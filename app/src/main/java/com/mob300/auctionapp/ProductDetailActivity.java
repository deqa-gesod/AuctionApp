package com.mob300.auctionapp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private EditText mProductBidPrice;
    private Button buttonBid;
    private FirebaseAuth mAuth;
    private DatabaseReference mProductRef;
    private String productId;
    private AlertDialog descriptionDialog;
    private ViewPager2 viewPagerImages; // Changed from ImageView to ViewPager2
    private ArrayList<String> imageUrls = new ArrayList<>();
    private Integer productPrice; // For storing the product's original or highest bid price
    private String fullProductDescription = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initFirebase();
        initViews();
        fetchProductDetails();
        listenForOfferedPriceChangesAndUpdatePotentialBuyer();




    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        productId = getIntent().getStringExtra("productId");
        mProductRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
    }


    private void initViews() {
        mProductBidPrice = findViewById(R.id.LastPrice);
        buttonBid = findViewById(R.id.contact_button);
        viewPagerImages = findViewById(R.id.viewPagerImages); // Initialization for ViewPager2

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        setupBidButton();

        TextView productDescriptionTextView = findViewById(R.id.product_description_text_view);
        productDescriptionTextView.setOnClickListener(v -> showFullProductDescription());

        // Removed setupImageNavigationButtons call
    }

    private void showFullProductDescription() {
        AlertDialog.Builder descriptionDialogBuilder = new AlertDialog.Builder(this, R.style.CustomProgressDialog);
        descriptionDialogBuilder.setMessage(fullProductDescription);

        descriptionDialog = descriptionDialogBuilder.create();

        AlertDialog descriptionDialog = descriptionDialogBuilder.create();

        // Setting the positive button listener directly on the dialog to ensure we have access to it
        descriptionDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = descriptionDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> descriptionDialog.dismiss());
            // Set the positive button text color


            // Attempt to change the message text color
            TextView messageView = descriptionDialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTypeface(null, Typeface.NORMAL);
                messageView.setTextColor(Color.parseColor("#4B5055"));
                messageView.setLineSpacing(0, 1.5f);
            }

        });

        descriptionDialog.show();
    }


    private void fetchProductDetails() {
        fetchProductBasicDetails();
        fetchImageUrls();
        fetchPotentialBuyerBid();
    }


    private void listenForOfferedPriceChangesAndUpdatePotentialBuyer() {
        DatabaseReference offeredPricesRef = mProductRef.child("offeredPrices");
        offeredPricesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mProductRef.child("potentialBuyer").removeValue();  // No bids left, remove potential buyer
                    return;
                }

                int maxBid = 0;
                String maxBidUserId = null;
                String fullName = null;

                // Iterate over all bids to find the highest one
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer bid = snapshot.child("price").getValue(Integer.class);
                    String userId = snapshot.child("userId").getValue(String.class);
                    String fullname = snapshot.child("fullName").getValue(String.class);
                    if (bid != null && bid > maxBid) {
                        maxBid = bid;
                        maxBidUserId = userId;
                        fullName = fullname;
                    }
                }

                // Update potential buyer with the highest bid found
                if (maxBidUserId != null) {
                    updatePotentialBuyer2(fullName, maxBid, maxBidUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to fetch offered prices: " + databaseError.getMessage());
            }
        });
    }

    private void updatePotentialBuyer2(String fullName, int maxBid, String userId) {
        Map<String, Object> potentialBuyer = new HashMap<>();
        potentialBuyer.put("maxBid", maxBid);
        potentialBuyer.put("fullName", fullName);
        potentialBuyer.put("userId", userId);

        mProductRef.child("potentialBuyer").setValue(potentialBuyer);
    }

    private void fetchPotentialBuyerBid() {
        mProductRef.child("potentialBuyer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer potentialBuyerBid = dataSnapshot.child("maxBid").getValue(Integer.class);
                if (potentialBuyerBid != null) {
                    productPrice = potentialBuyerBid; // Update the product price if there's a higher bid from the potential buyer
                    updateDisplayedPrice(potentialBuyerBid); // Update the UI with the new bid
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void fetchProductBasicDetails() {
        mProductRef.child("Product_details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateUIWithProductDetails(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to load product details.");
            }
        });
    }

    private void updateUIWithProductDetails(DataSnapshot dataSnapshot) {
        String productName = dataSnapshot.child("productName").getValue(String.class);
        productPrice = dataSnapshot.child("productPrice").getValue(Integer.class);
        String productDescription = dataSnapshot.child("productDescription").getValue(String.class);
        String currency = dataSnapshot.child("selectedCurrency").getValue(String.class);
        String cityAndCountry = dataSnapshot.child("countryAndCity").getValue(String.class);

        ((TextView) findViewById(R.id.product_name_text_view)).setText(productName);
        ((TextView) findViewById(R.id.product_description_text_view)).setText(productDescription);
        ((TextView) findViewById(R.id.CityAndCountry_Spinner_text_view)).setText(cityAndCountry);
        ((TextView) findViewById(R.id.currencySpinner1_text_view)).setText(currency);

        updateDisplayedPrice(null); // Initially display the product's price

        fullProductDescription = dataSnapshot.child("productDescription").getValue(String.class);
    }

    private void setupImageSlider() {
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls, false);
        viewPagerImages.setAdapter(adapter);
    }

    private void fetchImageUrls() {
        mProductRef.child("imageUrls").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    imageUrls.add(imageSnapshot.getValue(String.class));
                }
                setupImageSlider();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to load images.");
            }
        });
    }


    private void setupBidButton() {
        buttonBid.setOnClickListener(v -> {
            String bidPriceStr = mProductBidPrice.getText().toString().trim();
            if (bidPriceStr.isEmpty()) {
                showToast("Please enter a bid amount.");
                return;
            }
            try {
                int bidPrice = Integer.parseInt(bidPriceStr);
                submitBidToFirebase(bidPrice);
            } catch (NumberFormatException e) {
                showToast("Please enter a valid number.");
            }
        });
    }

    private void submitBidToFirebase(int bidPrice) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(mAuth.getCurrentUser().getUid()).child("fullName");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fullName = dataSnapshot.getValue(String.class);
                if (fullName != null) {
                    DatabaseReference potentialBuyerRef = mProductRef.child("potentialBuyer");
                    DatabaseReference offeredPricesRef = mProductRef.child("offeredPrices"); // Reference to the new node

                    potentialBuyerRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            Integer currentHighestBid = mutableData.child("maxBid").getValue(Integer.class);
                            if (currentHighestBid == null || bidPrice > currentHighestBid) {
                                mutableData.child("maxBid").setValue(bidPrice);
                                mutableData.child("fullName").setValue(fullName);

                                // Add bid to the offeredPrices node
                                Map<String, Object> bidDetails = new HashMap<>();
                                bidDetails.put("userId", mAuth.getCurrentUser().getUid());
                                bidDetails.put("fullName", fullName);
                                bidDetails.put("price", bidPrice);
                                offeredPricesRef.push().setValue(bidDetails); // Pushing the bid to offeredPrices

                                return Transaction.success(mutableData);
                            }
                            return Transaction.abort();
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed,
                                               @Nullable DataSnapshot dataSnapshot) {
                            if (committed) {
                                showToast("Your bid is successfully made.");
                                finish();
                            } else {
                                showToast("Your bid was not higher than the current highest bid.");
                            }
                        }
                    });
                } else {
                    showToast("Full Name not found in the database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Error retrieving full name: " + databaseError.getMessage());
            }
        });
    }

    private void updateDisplayedPrice(@Nullable Integer highestBid) {
        TextView currentPriceTextView = findViewById(R.id.current_price_label_text_view);
        String priceText = highestBid != null ? String.valueOf(highestBid) : productPrice != null ? String.valueOf(productPrice) : "Loading...";
        currentPriceTextView.setText(priceText);
    }

    private void showToast(String message) {
        Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (descriptionDialog != null && descriptionDialog.isShowing()) {
            descriptionDialog.dismiss();
        }
        super.onDestroy();
    }
}
