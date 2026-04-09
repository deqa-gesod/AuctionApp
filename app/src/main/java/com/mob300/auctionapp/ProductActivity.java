package com.mob300.auctionapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText mProductName, mProductPrice, mDeadline, mProductDescription, mContactInfo;
    private ImageView mProductImage;
    private Button mUploadButton;
    private ProgressDialog progressDialog;
    private Spinner mCurrencySpinner, mCountrySpinner, mCitySpinner;
    private List<Uri> mImageUris = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private boolean isUploading = false;
    private Map<String, List<String>> countryToCitiesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        initializeUi();
        setupListeners();
    }

    private void initializeUi() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = createProgressDialog();

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("products");
        mStorageRef = FirebaseStorage.getInstance().getReference("product_images");

        mProductName = findViewById(R.id.product_name);
        mProductPrice = findViewById(R.id.product_price);
        mDeadline = findViewById(R.id.editTextDate);
        mProductDescription = findViewById(R.id.product_description);
        mContactInfo = findViewById(R.id.contact_info);
        mProductImage = findViewById(R.id.product_image);
        mUploadButton = findViewById(R.id.upload_button);

        setupSpinners();
        initializeCountryCityData();
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this, R.style.CustomProgressDialog);
        progressDialog.setMessage("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        return progressDialog;
    }

    private void setupListeners() {
        mDeadline.setOnClickListener(v -> showDateTimeDialog(mDeadline));
        mProductImage.setOnClickListener(v -> openFileChooser());
        mUploadButton.setOnClickListener(v -> {
            if (!isUploading) uploadFile();
        });
    }


    public class CustomSpinnerAdapter extends ArrayAdapter<CharSequence> {
        public CustomSpinnerAdapter(@NonNull Context context, @NonNull List<CharSequence> objects) {
            super(context, R.layout.spinner_item_with_icon, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_with_icon, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.spinner_item_text);
            textView.setText(getItem(position));

            // Optionally customize the image view here if needed

            return convertView;
        }
    }


    private void setupSpinners() {
        mCurrencySpinner = findViewById(R.id.currencySpinner);
        mCountrySpinner = findViewById(R.id.countrySpinner);
        mCitySpinner = findViewById(R.id.citySpinner);

        List<CharSequence> currencyList = Arrays.asList(getResources().getStringArray(R.array.currency_array));
        // Now, use your CustomSpinnerAdapter
        CustomSpinnerAdapter currencyAdapter = new CustomSpinnerAdapter(this, currencyList);
        mCurrencySpinner.setAdapter(currencyAdapter);
        mCurrencySpinner.setBackground(null);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        List<CharSequence> countryList = Arrays.asList(getResources().getStringArray(R.array.country_array));
        CustomSpinnerAdapter CountryAdapter = new CustomSpinnerAdapter(this, countryList);
        CountryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountrySpinner.setAdapter(CountryAdapter);
        mCountrySpinner.setBackground(null);


        mCountrySpinner.setAdapter(CountryAdapter);
        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCitySpinner((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateCitySpinner(String selectedCountry) {
        List<String> cities;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            cities = countryToCitiesMap.getOrDefault(selectedCountry, Collections.emptyList());
        } else {
            cities = countryToCitiesMap.containsKey(selectedCountry) ? countryToCitiesMap.get(selectedCountry) : Collections.emptyList();
        }
        // Convert List<String> to List<CharSequence> as our CustomSpinnerAdapter expects it
        List<CharSequence> cityCharSequenceList = new ArrayList<>(cities);
        CustomSpinnerAdapter cityAdapter = new CustomSpinnerAdapter(this, cityCharSequenceList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCitySpinner.setAdapter(cityAdapter);
        mCitySpinner.setBackground(null); // If you want to remove the default spinner background
    }



    private void initializeCountryCityData() {
        countryToCitiesMap = new HashMap<>();
        countryToCitiesMap.put("Norway", Arrays.asList("Oslo", "Bergen", "Honefoss"));
        countryToCitiesMap.put("Kyrgyzstan", Arrays.asList("Bishkek", "Osh", "Batken"));
        countryToCitiesMap.put("Tajikistan", Arrays.asList("Dushanbe", "Khorog", "Khujand"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUris != null && !mImageUris.isEmpty()) {
            isUploading = true;
            mUploadButton.setEnabled(false);

            progressDialog.setProgress(0); // Initialize progress to 0
            progressDialog.setMax(mImageUris.size()); // Set the maximum progress value

            progressDialog.show(); // Show the progress dialog



            List<String> uploadedImageUrls = new ArrayList<>();

            for (Uri imageUri : mImageUris) {
                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                        + "." + getFileExtension(imageUri));

                fileReference.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        uploadedImageUrls.add(uri.toString());
                                        // Check if all images are uploaded
                                        if (uploadedImageUrls.size() == mImageUris.size()) {
                                            // Handle saving product to the database with the list of image URLs
                                            saveProductToDatabase(uploadedImageUrls);
                                        }
                                    }
                                });

                                int progress = progressDialog.getProgress() + 1;
                                progressDialog.setProgress(progress);


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                isUploading = false;
                                mUploadButton.setEnabled(true);
                            }
                        });
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }



    private void saveProductToDatabase(List<String> imageUrls) {
        String productName = mProductName.getText().toString().trim();
        String productPrice = mProductPrice.getText().toString().trim();
        String deadline = mDeadline.getText().toString().trim();
        String productDescription = mProductDescription.getText().toString().trim();
        String contactInfo = mContactInfo.getText().toString().trim();
        String selectedCurrency = mCurrencySpinner.getSelectedItem().toString();
        String selectedCountry = mCountrySpinner.getSelectedItem().toString();
        String selectedCity = mCitySpinner.getSelectedItem().toString();

        // Combine the country and city into a single string for location
        String countryAndCity = selectedCountry + ", " + selectedCity;

        String productId = mDatabaseRef.push().getKey();
        String userId = mAuth.getCurrentUser().getUid(); // Get the current user's ID


        // Create a map for product details, now including the userId
        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("productName", productName);
        float price;
        try {
            price = Float.parseFloat(productPrice);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price.", Toast.LENGTH_SHORT).show();
            return;
        }
        productDetails.put("productPrice", price);
        productDetails.put("deadline", deadline);
        productDetails.put("productDescription", productDescription);
        productDetails.put("contactInfo", contactInfo);
        productDetails.put("selectedCurrency", selectedCurrency);
        productDetails.put("countryAndCity", countryAndCity);
        productDetails.put("userId", userId); // Include the user ID who posted the product
        productDetails.put("productID", productId);

        // Save product details including the user ID
        mDatabaseRef.child(productId).child("Product_details").setValue(productDetails);

        // Save image URLs
        Map<String, Object> imageUrlsMap = new HashMap<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            imageUrlsMap.put("imageUrl" + (i + 1), imageUrls.get(i));
        }
        mDatabaseRef.child(productId).child("imageUrls").setValue(imageUrlsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProductActivity.this, "Product uploaded successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ProductActivity.this, "Failed to upload product", Toast.LENGTH_SHORT).show();
                        }
                        isUploading = false; // Set the flag to false after the upload is complete
                        mUploadButton.setEnabled(true); // Enable the upload button
                        progressDialog.dismiss();
                    }
                });
    }



    private void showDateTimeDialog(final EditText editTextDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        editTextDate.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };

                new TimePickerDialog(ProductActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        };

        new DatePickerDialog(ProductActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int itemCount = data.getClipData().getItemCount();
                for (int i = 0; i < itemCount; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    mImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {  // Note the "else" here
                mImageUris.add(data.getData());
            }

            // Display the first image in the list or handle as needed
            Picasso.get().load(mImageUris.get(0)).into(mProductImage);
        }
    }
}


