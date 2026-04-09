package com.mob300.auctionapp;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private Context context;
    private List<Service> serviceList;
    List<Service> filteredServiceList;

    public ServiceAdapter(Context context, List<Service> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
        this.filteredServiceList = new ArrayList<>(serviceList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.service_item, parent, false);
        return new ViewHolder(view);
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<Service> filteredList = new ArrayList<>();

                if (query.isEmpty()) {
                    filteredList.addAll(serviceList);
                } else {
                    for (Service service : serviceList) {
                        if (service.getServiceName().toLowerCase().contains(query) || service.getServiceDescription().toLowerCase().contains(query)) {
                            filteredList.add(service);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredServiceList.clear();
                filteredServiceList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service serviceModel = filteredServiceList.get(position);
        holder.serviceName.setText(serviceModel.getServiceName());
        holder.serviceDescription.setText(serviceModel.getServiceDescription());
        holder.contactInfo.setText(serviceModel.getContactInfo());

        // Apply RequestOptions for Glide to display rounded images
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new RoundedCorners(250)); // Change the value to adjust the corner radius


        Glide.with(context)
                .load(serviceModel.getImageUrl())
                .apply(requestOptions)
                .into(holder.serviceImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredServiceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, serviceDescription, contactInfo;
        ImageView serviceImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceName = itemView.findViewById(R.id.service_name);
            serviceDescription = itemView.findViewById(R.id.service_description);
            contactInfo = itemView.findViewById(R.id.contact_info);
            serviceImage = itemView.findViewById(R.id.service_image);
        }
    }
}
