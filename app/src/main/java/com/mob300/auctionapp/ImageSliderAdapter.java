package com.mob300.auctionapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private List<String> imageUrls;
    private LayoutInflater inflater;
    private boolean isFullScreen; // Add this line

    public ImageSliderAdapter(Context context, List<String> imageUrls, boolean isFullScreen) { // Add parameter
        this.imageUrls = imageUrls;
        this.inflater = LayoutInflater.from(context);
        this.isFullScreen = isFullScreen; // Set the flag
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.image_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(holder.imageView.getContext()).load(imageUrl).into(holder.imageView);

        if (!isFullScreen) {
            holder.imageView.setOnClickListener(v -> FullScreenImageActivity.start(holder.imageView.getContext(), new ArrayList<>(imageUrls), position, true));
        } else {
            holder.imageView.setOnClickListener(null); // Disables click in full-screen mode
        }
    }





    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgView);
        }
    }
}
