package com.mob300.auctionapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.Handler;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Context context;
    private List<Product> productList;
    private FirebaseAuth mAuth;
    private ProductManager productManager;
    private FirebaseDatabase mDatabase;
    List<Product> filteredProductList;

    private void updateTimeLeft(final String deadlineString, final TextView timeLeftView) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date deadline = dateFormat.parse(deadlineString);
                    Date now = new Date();

                    long diff = deadline.getTime() - now.getTime();
                    if (diff > 0) {
                        long seconds = diff / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;
                        long days = hours / 24;

                        hours %= 24;
                        minutes %= 60;


                        String timeLeft = days + "d " + hours + "h " + minutes + "m " ;
                        timeLeftView.setText(timeLeft);

                        handler.postDelayed(this, 1000); // update every second
                    } else {
                        productManager.moveExpiredProducts();
                    }
                } catch (Exception e) {
                    timeLeftView.setText("Error");
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.filteredProductList = new ArrayList<>(productList);
        this.productManager = new ProductManager();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.produc_item_layout, parent, false);
        return new ViewHolder(view);
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<Product> filteredList = new ArrayList<>();

                if (query.isEmpty()) {
                    filteredList.addAll(productList);
                } else {
                    for (Product product : productList) {
                        if (product.getProductName().toLowerCase().contains(query) || product.getProductDescription().toLowerCase().contains(query)) {
                            filteredList.add(product);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredProductList.clear();
                filteredProductList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product productModel = filteredProductList.get(position);
        holder.productName.setText(productModel.getProductName());
        holder.selectedCurrency.setText(String.valueOf(productModel.getSelectedCurrency()));
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        updateTimeLeft(productModel.getDeadline(), holder.Deadline);

        // Reference to check for the product within the 'products' node
        DatabaseReference productRef = mDatabase.getReference("products").child(productModel.getProductId());

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("potentialBuyer/maxBid")) {
                        // If there's a potentialBuyer with a maxBid, use it
                        int maxBid = snapshot.child("potentialBuyer/maxBid").getValue(Integer.class);
                        holder.productPriceVar.setText(String.valueOf(maxBid));
                    } else if (snapshot.hasChild("Product_details/productPrice")) {
                        // If there's no potentialBuyer but there are Product_details, use the productPrice
                        int productPrice = snapshot.child("Product_details/productPrice").getValue(Integer.class);
                        holder.productPriceVar.setText(String.valueOf(productPrice));
                    } else {
                        // Handle the case where neither node is available
                        holder.productPriceVar.setText("N/A"); // Or any default/fallback handling
                    }
                } else {
                    // Handle the case where the product does not exist in the database
                    holder.productPriceVar.setText("N/A"); // Or any default/fallback handling
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors.
                holder.productPriceVar.setText("Error");
            }
        });

        RequestOptions requestOptions = new RequestOptions().transforms(new RoundedCorners(16));
        if (productModel.getImageUrls() != null && !productModel.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(productModel.getImageUrls().get(0))
                    .apply(requestOptions)
                    .into(holder.productImage);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
            String productId = filteredProductList.get(holder.getAdapterPosition()).getProductId();
            intent.putExtra("productId", productId);
            view.getContext().startActivity(intent);
        });
    }






    @Override
    public int getItemCount() {
        return filteredProductList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName;
        private TextView Deadline;
        private TextView productPriceVar;
        private TextView selectedCurrency;
        private ImageView productImage;





        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.ivProductImage);
            productName = itemView.findViewById(R.id.tvProductName);
            Deadline = itemView.findViewById(R.id.tvDeadline);
            productPriceVar = itemView.findViewById(R.id.tvContactInfo);
            selectedCurrency = itemView.findViewById(R.id.selectedCurrency);
        }
    }
}

