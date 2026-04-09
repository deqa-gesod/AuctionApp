package com.mob300.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuyProductsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_products);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = findViewById(R.id.recyclerV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        // Adjusted reference to fetch products with new database structure
        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        fetchProducts();

        adapter = new ProductAdapter(BuyProductsActivity.this, productList);
        recyclerView.setAdapter(adapter);
        setUpSearch();
    }

    private void fetchProducts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    // Assuming Product_details is a child of the product ID
                    DataSnapshot productDetailSnapshot = productSnapshot.child("Product_details");
                    Product product = productDetailSnapshot.getValue(Product.class);
                    if (product != null) {
                        // Assuming you have a method in your Product class to set the product ID
                        product.setProductId(productSnapshot.getKey());
                        // Assuming you have a method in your Product class to handle image URLs
                        List<String> imageUrls = new ArrayList<>();
                        DataSnapshot imageUrlSnapshot = productSnapshot.child("imageUrls");
                        for (DataSnapshot urlSnapshot : imageUrlSnapshot.getChildren()) {
                            imageUrls.add(urlSnapshot.getValue(String.class));
                        }
                        product.setImageUrls(imageUrls);
                        productList.add(product);
                    }
                }
                adapter.filteredProductList.clear();
                adapter.filteredProductList.addAll(productList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BuyProductsActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpSearch() {
        EditText searchProducts = findViewById(R.id.search_products);
        searchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
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
