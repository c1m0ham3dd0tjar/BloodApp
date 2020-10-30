package com.example.licenta.viewholder;

import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta.R;

public class DonationCenterViewHolder extends RecyclerView.ViewHolder implements Filterable {

    public TextView tvName, tvAdress, tvProgram,  tvPhoneNumbers, tvShowState, tvShowDistance;
    public ImageView btnCollapseCard;
    public ConstraintLayout parentLayout;
    public ConstraintLayout expendableLayout;
    public ImageView imgAdress, imgNavigation;

    public DonationCenterViewHolder(@NonNull View itemView) {
        super(itemView);

        tvName = itemView.findViewById(R.id.tvCenterName);
        tvAdress = itemView.findViewById(R.id.tvCenterAdress);
        tvProgram = itemView.findViewById(R.id.tvProgram);
        tvPhoneNumbers = itemView.findViewById(R.id.tvPhoneNumbers);
        tvShowState = itemView.findViewById(R.id.tvShowState);
        tvShowDistance = itemView.findViewById(R.id.tvShowDistance);
        parentLayout = itemView.findViewById(R.id.parentLayout);
        expendableLayout = itemView.findViewById(R.id.collapsibleLayout);
        btnCollapseCard = itemView.findViewById(R.id.btnColapseCard);
        imgAdress = itemView.findViewById(R.id.imgAdress);
        imgNavigation = itemView.findViewById(R.id.imgNavigation);


    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {

                }

                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }
}
