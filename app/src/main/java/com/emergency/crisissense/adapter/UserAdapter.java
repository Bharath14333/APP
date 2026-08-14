package com.emergency.crisissense.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final List<User> usersList;
    private final Context context;

    public UserAdapter(List<User> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());
        holder.txtPhone.setText(user.getPhone());
        
        String role = user.getRole().toUpperCase();
        holder.txtRole.setText(role);

        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName, txtEmail, txtPhone, txtRole;
        public ImageView imgAvatar;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_user_name);
            txtEmail = itemView.findViewById(R.id.txt_user_email);
            txtPhone = itemView.findViewById(R.id.txt_user_phone);
            txtRole = itemView.findViewById(R.id.txt_user_role);
            imgAvatar = itemView.findViewById(R.id.img_user_avatar);
        }
    }
}
