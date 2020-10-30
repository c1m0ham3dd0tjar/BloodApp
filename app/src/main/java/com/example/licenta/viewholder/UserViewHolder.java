package com.example.licenta.viewholder;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta.R;

public class UserViewHolder extends RecyclerView.ViewHolder {

    public TextView tvUserName;
    public TextView tvUserBloodType;
    public TextView tvUserBloodType2;

    public UserViewHolder(@NonNull View itemView) {

        super(itemView);

        tvUserName = itemView.findViewById(R.id.tvUserName);
        tvUserBloodType = itemView.findViewById(R.id.tvUserBloodType);
        tvUserBloodType2 = itemView.findViewById(R.id.tvUserBloodType2);
    }
}
