package com.mob300.auctionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MyOffersAdapter extends RecyclerView.Adapter<MyOffersAdapter.MyOffersViewHolder> {

    private Context context;
//    private List<Offer> offerList;
    private List<Product> mOffers;

    public MyOffersAdapter(Context context, List<Product> mOffers) {
        this.context = context;
        this.mOffers = mOffers;
    }

    @NonNull
    @Override
    public MyOffersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_offers_item, parent, false);
        return new MyOffersViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyOffersViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (mOffers == null || mOffers.isEmpty()) {
            // Handle null or empty list (show loading indicator or return)
            return;
        }


        Product product = mOffers.get(position);

        holder.offerName.setText(product.getName());

        Glide.with(context)
                .load(product.getImageUrls().get(0)) // Load the first image from the list
                .into(holder.offerImages);

        holder.editButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reference to the offeredPrices node under the specific product
                Log.d("MyOffersAdapter", "Product ID: " + product.getProductId());

                DatabaseReference offeredPricesRef = FirebaseDatabase.getInstance().getReference("products")
                        .child(product.getProductId()).child("offeredPrices");

                // Query to find bids by the current user
                offeredPricesRef.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    // This will remove each bid made by the user
                                    snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Your bid was successfully withdrawn", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Failed to withdraw bid: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(context, "Failed to delete bid: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }


    @Override
    public int getItemCount() {
        return mOffers.size();
    }

    public void deleteItem(int position) {
        mOffers.remove(position);
        notifyItemRemoved(position);
    }

    public class MyOffersViewHolder extends RecyclerView.ViewHolder {
        TextView offerName;
        ImageView offerImages; // Change the type to ImageView
        Button editButton, deleteButton;

        public MyOffersViewHolder(@NonNull View itemView) {
            super(itemView);
            offerName = itemView.findViewById(R.id.offer_name);
            offerImages = itemView.findViewById(R.id.offer_image); // Change the type to ImageView
            editButton = itemView.findViewById(R.id.offer_edit_button);
            deleteButton = itemView.findViewById(R.id.offer_delete_button);
        }
    }
}
