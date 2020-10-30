package com.example.licenta.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta.R;
import com.google.android.material.button.MaterialButton;

public class AppointmentViewHolder extends RecyclerView.ViewHolder {

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    public TextView tvDonationCenter, tvPatientName, tvContactNumber, tvDate;
    public MaterialButton btnConfirmDonation, btnDeleteAppointment, btnEditAppointment;
    public ImageView imgDonationConfirmed;

    public AppointmentViewHolder(@NonNull View itemView) {
        super(itemView);

        tvDonationCenter = itemView.findViewById(R.id.tvDonCenter);
        tvPatientName = itemView.findViewById(R.id.tvPatient);
        tvContactNumber = itemView.findViewById(R.id.tvPhone);
        tvDate = itemView.findViewById(R.id.tvDate);
        btnConfirmDonation = itemView.findViewById(R.id.btnConfirmDonation);
        btnDeleteAppointment = itemView.findViewById(R.id.btnDeleteAppointment);
        btnEditAppointment = itemView.findViewById(R.id.btnEditAppointment);
        imgDonationConfirmed = itemView.findViewById(R.id.imgDonationConfirmed);
        imgDonationConfirmed.setVisibility(View.INVISIBLE);

    }
}
