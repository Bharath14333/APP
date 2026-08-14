package com.emergency.crisissense.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.EmergencyService;
import java.util.List;
import java.util.Set;

public class EmergencyServicesAdapter extends RecyclerView.Adapter<EmergencyServicesAdapter.ViewHolder> {

    public interface OnServiceClickListener {
        void onCallClick(EmergencyService service);
        void onDirectionsClick(EmergencyService service);
        void onFavoriteClick(EmergencyService service, boolean isFavorite);
    }

    private final List<EmergencyService> services;
    private final Set<String> favoriteIds;
    private final OnServiceClickListener listener;
    private final Context context;

    public EmergencyServicesAdapter(List<EmergencyService> services, Set<String> favoriteIds, 
                                    OnServiceClickListener listener, Context context) {
        this.services = services;
        this.favoriteIds = favoriteIds;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyService service = services.get(position);
        holder.txtName.setText(service.getName());
        holder.txtAddress.setText(service.getAddress());
        holder.txtPhone.setText("Ph: " + service.getPhone());
        
        // Show category icon
        int iconRes = android.R.drawable.ic_menu_compass; // fallback
        int iconColor = context.getResources().getColor(R.color.primary);
        
        String category = service.getCategory();
        if ("Hospital".equalsIgnoreCase(category)) {
            iconRes = android.R.drawable.ic_menu_myplaces;
            iconColor = context.getResources().getColor(R.color.severity_medium); // green/orange
        } else if ("Police".equalsIgnoreCase(category)) {
            iconRes = android.R.drawable.ic_lock_idle_lock;
            iconColor = context.getResources().getColor(R.color.primary);
        } else if ("Fire".equalsIgnoreCase(category)) {
            iconRes = android.R.drawable.ic_menu_report_image;
            iconColor = context.getResources().getColor(R.color.emergency_red);
        } else if ("Ambulance".equalsIgnoreCase(category)) {
            iconRes = android.R.drawable.ic_menu_call;
            iconColor = context.getResources().getColor(R.color.severity_high);
        } else if ("Pharmacy".equalsIgnoreCase(category)) {
            iconRes = android.R.drawable.ic_menu_add;
            iconColor = context.getResources().getColor(R.color.severity_low);
        } else if ("Shelter".equalsIgnoreCase(category)) {
            iconRes = android.R.drawable.ic_menu_compass;
            iconColor = context.getResources().getColor(R.color.secondary);
        }
        
        holder.imgIcon.setImageResource(iconRes);
        holder.imgIcon.setColorFilter(iconColor);

        // Distance text will be updated dynamically in Activity based on user GPS
        holder.txtDistance.setVisibility(View.GONE); 

        // Favorite Star representation
        final boolean isFav = favoriteIds.contains(service.getServiceId());
        holder.btnFavorite.setImageResource(isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        holder.btnFavorite.setOnClickListener(v -> {
            boolean nextFavState = !isFav;
            if (nextFavState) {
                favoriteIds.add(service.getServiceId());
            } else {
                favoriteIds.remove(service.getServiceId());
            }
            notifyItemChanged(position);
            listener.onFavoriteClick(service, nextFavState);
        });

        holder.btnCall.setOnClickListener(v -> listener.onCallClick(service));
        holder.btnDirections.setOnClickListener(v -> listener.onDirectionsClick(service));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtAddress, txtDistance, txtPhone;
        ImageView imgIcon;
        ImageButton btnFavorite, btnCall, btnDirections;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_service_name);
            txtAddress = itemView.findViewById(R.id.txt_service_address);
            txtDistance = itemView.findViewById(R.id.txt_service_distance);
            txtPhone = itemView.findViewById(R.id.txt_service_phone);
            imgIcon = itemView.findViewById(R.id.img_service_icon);
            btnFavorite = itemView.findViewById(R.id.btn_favorite_service);
            btnCall = itemView.findViewById(R.id.btn_call_service);
            btnDirections = itemView.findViewById(R.id.btn_directions_service);
        }
    }
}
