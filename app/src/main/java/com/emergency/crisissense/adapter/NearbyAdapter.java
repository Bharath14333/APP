package com.emergency.crisissense.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.NearbyPlace;
import java.util.List;

public class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.ViewHolder> {
    private final List<NearbyPlace> nearbyList;
    private final Context context;

    public NearbyAdapter(List<NearbyPlace> nearbyList, Context context) {
        this.nearbyList = nearbyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nearby_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NearbyPlace place = nearbyList.get(position);
        holder.txtName.setText(place.getName());
        holder.txtAddress.setText(place.getAddress());
        holder.txtDistance.setText(place.getDistance());
        holder.txtPhone.setText("Ph: " + place.getPhone());

        // Set Icon based on type
        String type = place.getType().toUpperCase();
        if (type.contains("HOSPITAL")) {
            holder.imgIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
        } else if (type.contains("POLICE")) {
            holder.imgIcon.setImageResource(android.R.drawable.ic_menu_compass);
        } else {
            holder.imgIcon.setImageResource(android.R.drawable.ic_dialog_alert);
        }

        holder.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + place.getPhone()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return nearbyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName, txtAddress, txtDistance, txtPhone;
        public ImageView imgIcon;
        public ImageButton btnCall;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_service_name);
            txtAddress = itemView.findViewById(R.id.txt_service_address);
            txtDistance = itemView.findViewById(R.id.txt_service_distance);
            txtPhone = itemView.findViewById(R.id.txt_service_phone);
            imgIcon = itemView.findViewById(R.id.img_service_icon);
            btnCall = itemView.findViewById(R.id.btn_call_service);
        }
    }
}
