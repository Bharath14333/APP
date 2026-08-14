package com.emergency.crisissense.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.emergency.crisissense.R;
import com.emergency.crisissense.activity.IncidentDetailsActivity;
import com.emergency.crisissense.model.Incident;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.ViewHolder> {
    private final List<Incident> incidentsList;
    private final Context context;

    public IncidentAdapter(List<Incident> incidentsList, Context context) {
        this.incidentsList = incidentsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incident, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Incident incident = incidentsList.get(position);
        holder.txtTitle.setText(incident.getTitle());
        holder.txtDesc.setText(incident.getDescription());
        holder.txtType.setText(incident.getType());
        
        String severity = incident.getSeverity().toUpperCase();
        holder.txtSeverity.setText(severity);
        
        // Severity color settings
        int severityColor;
        switch (severity) {
            case "LOW":
                severityColor = Color.parseColor("#4CAF50");
                break;
            case "MEDIUM":
                severityColor = Color.parseColor("#FF9800");
                break;
            case "HIGH":
                severityColor = Color.parseColor("#E65100");
                break;
            case "CRITICAL":
            default:
                severityColor = Color.parseColor("#D50000");
                break;
        }
        holder.txtSeverity.setTextColor(severityColor);

        // Status tag settings
        String status = incident.getStatus().toUpperCase();
        holder.txtStatus.setText(incident.getStatus());
        int statusColor;
        switch (status) {
            case "APPROVED":
                statusColor = Color.parseColor("#1E88E5");
                break;
            case "REJECTED":
                statusColor = Color.parseColor("#E53935");
                break;
            case "RESOLVED":
                statusColor = Color.parseColor("#43A047");
                break;
            case "PENDING":
            default:
                statusColor = Color.parseColor("#FFB300");
                break;
        }
        holder.txtStatus.setBackgroundColor(statusColor);

        // Date settings
        if (incident.getCreatedDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            holder.txtDate.setText(sdf.format(incident.getCreatedDate()));
        } else {
            holder.txtDate.setText("");
        }

        // Image loading
        if (incident.getImage() != null && !incident.getImage().isEmpty()) {
            Glide.with(context)
                .load(incident.getImage())
                .placeholder(android.R.drawable.ic_dialog_alert)
                .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_dialog_alert);
        }

        // Details click action
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, IncidentDetailsActivity.class);
            intent.putExtra("incident", incident);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return incidentsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTitle, txtDesc, txtType, txtSeverity, txtDate, txtStatus;
        public ImageView imgThumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txt_incident_title);
            txtDesc = itemView.findViewById(R.id.txt_incident_desc);
            txtType = itemView.findViewById(R.id.txt_incident_type);
            txtSeverity = itemView.findViewById(R.id.txt_incident_severity);
            txtDate = itemView.findViewById(R.id.txt_incident_date);
            txtStatus = itemView.findViewById(R.id.txt_incident_status);
            imgThumbnail = itemView.findViewById(R.id.img_incident_thumbnail);
        }
    }
}
