package com.emergency.crisissense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.LogEntry;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private final List<LogEntry> logsList;

    public LogAdapter(List<LogEntry> logsList) {
        this.logsList = logsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LogEntry log = logsList.get(position);
        holder.txtAction.setText(log.getAction());
        holder.txtDetails.setText(log.getDetails());
        holder.txtPerformer.setText("Action by: " + log.getPerformedBy());

        if (log.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault());
            holder.txtTime.setText(sdf.format(log.getTimestamp()));
        } else {
            holder.txtTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return logsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtAction, txtDetails, txtPerformer, txtTime;

        public ViewHolder(View itemView) {
            super(itemView);
            txtAction = itemView.findViewById(R.id.txt_log_action);
            txtDetails = itemView.findViewById(R.id.txt_log_details);
            txtPerformer = itemView.findViewById(R.id.txt_log_performer);
            txtTime = itemView.findViewById(R.id.txt_log_time);
        }
    }
}
