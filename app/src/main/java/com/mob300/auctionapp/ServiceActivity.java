package com.mob300.auctionapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import java.util.Locale;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ServiceActivity extends AppCompatActivity {

    private TableLayout tableLayout1;
    private TableLayout tableLayout2;
    private DatabaseReference productsRef;
    private DatabaseReference productsRef2;
    private ScrollView scrollViewTable1, scrollViewTable2;
    private Button buttonTable1, buttonTable2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        scrollViewTable1 = findViewById(R.id.scrollView);
        scrollViewTable2 = findViewById(R.id.scrollView2);
        buttonTable1 = findViewById(R.id.btnCurrentTable);
        buttonTable2 = findViewById(R.id.btnSecondTable);
        tableLayout1 = findViewById(R.id.tableLayout1);
        tableLayout2 = findViewById(R.id.tableLayout2);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");
        productsRef2 = database.getReference("sold_products");

        ImageView downloadIcon = findViewById(R.id.download_icon);
        downloadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadOptionsDialog();
            }
        });


        scrollViewTable1.setVisibility(View.VISIBLE);
        scrollViewTable2.setVisibility(View.GONE);
        setActiveButton(buttonTable1, buttonTable2);

        fetchFirebaseData(tableLayout1);

        buttonTable1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollViewTable1.setVisibility(View.VISIBLE);
                scrollViewTable2.setVisibility(View.GONE);
                setActiveButton(buttonTable1, buttonTable2);
                fetchFirebaseData(tableLayout1);
            }
        });

        buttonTable2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollViewTable1.setVisibility(View.GONE);
                scrollViewTable2.setVisibility(View.VISIBLE);
                setActiveButton(buttonTable2, buttonTable1);
                fetchSecondTableData(tableLayout2);
            }
        });

        ImageView internetIcon = findViewById(R.id.internet_icon);
        internetIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the ClipboardManager to copy the link to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", "YOUR_AIRTABLE_INVITE_LINK");
                clipboard.setPrimaryClip(clip);

                // Show a message to the user indicating the link has been copied
                Toast.makeText(ServiceActivity.this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDownloadOptionsDialog() {
        CharSequence options[] = new CharSequence[]{"Purchased Products", "Current Bidding"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceActivity.this, R.style.CustomProgressDialog);


        builder.setTitle("Choose table to download:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Pass "purchased" as a standardized table type identifier
                    downloadTableData("purchased");
                } else if (which == 1) {
                    // Pass "bidding" as a standardized table type identifier
                    downloadTableData("bidding");
                }
            }
        });
        builder.show();
    }


    private void downloadTableData(String tableType) {
        DatabaseReference ref = tableType.equals("purchased") ? productsRef2 : productsRef;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> dataList = new ArrayList<>();
                // Adjust the header to include the required fields
                dataList.add("Product Name, Full Name, Price, Product ID");

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productId = productSnapshot.getKey();
                    // Fetch the productName from Product_details node
                    String productName = productSnapshot.child("Product_details/productName").getValue(String.class);
                    String currency = productSnapshot.child("Product_details/selectedCurrency").getValue(String.class);

                    productName = productName != null ? productName : "Unknown";
                    currency = currency != null ? currency : "";

                    // Assuming potentialBuyer exists within the same level as offeredPrices
                    DataSnapshot potentialBuyerSnapshot = productSnapshot.child("potentialBuyer");
                    Long bidValue = potentialBuyerSnapshot.child("maxBid").getValue(Long.class);
                    String fullName = potentialBuyerSnapshot.child("fullName").getValue(String.class);

                    // Ensure bidValue is properly handled
                    String maxBid = (bidValue != null ? bidValue.toString() : "Unknown") + " " + currency;
                    fullName = fullName != null ? fullName : "Unknown";

                    // Correcting the dataRow construction
                    String dataRow = "\"" + productName + "\",\"" + fullName + "\",\"" + maxBid + "\",\"" + formatAsExcelString(productId) + "\"";
                    dataList.add(dataRow);
                }

                // Simplified date format for file name uniqueness
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String date = sdf.format(new Date());
                String fileName = "Purchased_" + date + ".csv"; // Adjusted to reflect the specific file type being saved

                // Save the CSV data to a file
                saveDataToFile(dataList, fileName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DownloadData", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }




    private void saveDataToFile(ArrayList<String> dataList, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // Use the file name directly
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        }

        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                for (String dataRow : dataList) {
                    if (out != null) {
                        out.write((dataRow + "\n").getBytes());
                    }
                }
                Toast.makeText(this, "File saved to Downloads", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("FileSaveError", "Failed to save file", e);
                Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }


    private String formatAsExcelString(String value) {
        // This wraps the value in Excel's T() function to ensure it's treated as text.
        // T() returns the text unchanged if it's text; otherwise, it returns an empty string.
        // This method avoids Excel interpreting the value as a formula or command.
        return "=T(\"" + value.replace("\"", "\"\"") + "\")"; // Escaping internal quotes
    }
    
    private void setActiveButton(Button active, Button inactive) {
        // Apply the new drawable resources directly
        active.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_button));
        active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        inactive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.inactive_button));
        inactive.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
    }

    private void fetchSecondTableData(TableLayout tableLayout2) {
        productsRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int index = 1;

                tableLayout2.removeAllViews();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {

                    String productName1 = productSnapshot.child("Product_details").child("productName").getValue(String.class);
                    String fullName = productSnapshot.child("potentialBuyer").child("fullName").getValue(String.class);
                    Integer maxbidString = productSnapshot.child("potentialBuyer").child("maxBid").getValue(Integer.class);
                    String maxbid = String.valueOf(maxbidString);
                    String currency = productSnapshot.child("Product_details").child("selectedCurrency").getValue(String.class);


                    if (productName1 != null && fullName != null) {
                        TableRow row = new TableRow(getApplicationContext());

                        TextView indexTextView = createTextView(String.valueOf(index++), false);
                        row.addView(indexTextView);

                        // Product Name TextView
                        TextView productNameTextView2 = createTextView(productName1, false);
                        row.addView(productNameTextView2);

                        TextView fullNameTextView = createTextView(fullName, false);
                        row.addView(fullNameTextView);

                        String concatenate = maxbid + " " + currency;

                        // Full Name TextView
                        TextView price_currence = createTextView(concatenate, false);
                        row.addView(price_currence);

                        tableLayout2.addView(row);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void fetchFirebaseData(TableLayout tableLayout) {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableLayout.removeAllViews();
                int index = 1; // Start indexing from 1

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productId = productSnapshot.getKey(); // Retrieve the product ID
                    String productName = productSnapshot.child("Product_details").child("productName").getValue(String.class);
                    String fullName = productSnapshot.child("potentialBuyer").child("fullName").getValue(String.class);
                    Integer maxbidString = productSnapshot.child("potentialBuyer").child("maxBid").getValue(Integer.class);
                    String maxbid = String.valueOf(maxbidString);
                    String currency = productSnapshot.child("Product_details").child("selectedCurrency").getValue(String.class);

                    if (productName != null && fullName != null) {
                        TableRow row = new TableRow(getApplicationContext());
                        TextView indexTextView = createTextView(String.valueOf(index++), false);
                        row.addView(indexTextView);

                        // Product Name TextView
                        TextView productNameTextView = createTextView(productName, true);
                        if (productId != null) { // Ensure productId is not null
                            productNameTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showBiddingHistory(productId); // Pass productId to show bidding history
                                }
                            });
                        }
                        row.addView(productNameTextView);

                        TextView fullNameTextView = createTextView(fullName, false);
                        row.addView(fullNameTextView);

                        String concatenate = maxbid + " " + currency;

                        // Full Name TextView
                        TextView price_currence = createTextView(concatenate, false);
                        row.addView(price_currence);

                        tableLayout.addView(row);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void showBiddingHistory(String productId) {
        Log.d("BiddingHistory", "Fetching bids for product ID: " + productId);
        DatabaseReference bidsRef = productsRef.child(productId).child("offeredPrices");
        bidsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> bidHistory = new ArrayList<>();
                for (DataSnapshot bidSnapshot : dataSnapshot.getChildren()) {
                    String bidderName = bidSnapshot.child("fullName").getValue(String.class);
                    Integer bidAmount = bidSnapshot.child("price").getValue(Integer.class);

                    // Check if bidderName and bidAmount are not null
                    if (bidderName != null && bidAmount != null) {
                        // Add bidder's name and bid amount to the history
                        bidHistory.add(bidderName + ": " + bidAmount);
                    }
                }

                // Log the result and display the bid history
                if (bidHistory.isEmpty()) {
                    Log.d("BiddingHistory", "No bids found for this product.");
                } else {
                    Log.d("BiddingHistory", "Bids found: " + bidHistory.toString());
                }

                displayBidHistory(bidHistory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("BiddingHistory", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }



    private void displayBidHistory(ArrayList<String> bidHistory) {
        // Check if the bidHistory is not empty before displaying
        if (!bidHistory.isEmpty()) {
            // Use an AlertDialog to show the bid history
            AlertDialog.Builder builder = new AlertDialog.Builder(ServiceActivity.this, R.style.CustomProgressDialog);
            builder.setTitle("Bid History");

            // Create a custom ArrayAdapter
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ServiceActivity.this, android.R.layout.simple_list_item_1, bidHistory) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    // Let ArrayAdapter handle the View recycling
                    View view = super.getView(position, convertView, parent);
                    TextView textView = view.findViewById(android.R.id.text1);
                    // Set the text to bold
                    textView.setTypeface(null, Typeface.BOLD);
                    return view;
                }
            };

            // Use the custom ArrayAdapter to display each item
            builder.setAdapter(adapter, null);

            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.create();
            dialog.show();

            // Change the positive button text color to white
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(Color.WHITE);
            }
        } else {
            // Handle case where there are no bids
            Toast.makeText(ServiceActivity.this, "No bid history available for this product.", Toast.LENGTH_SHORT).show();
        }
    }



    private TextView createTextView(String text, boolean clickable) {
        TextView textView = new TextView(getApplicationContext());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundResource(R.drawable.cell_boder);

        if (clickable) {
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            textView.setClickable(true);
            textView.setFocusable(true);
        }
        return textView;
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
