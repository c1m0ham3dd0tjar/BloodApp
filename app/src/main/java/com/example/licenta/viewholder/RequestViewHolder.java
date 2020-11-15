package com.example.licenta.viewholder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.licenta.R;
import com.google.android.material.button.MaterialButton;

public class RequestViewHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle, tvCompatibleBloodTypes, tvUnitsNeeded, tvShowRequiredDate;
    public String unitsNeeded, bloodtype, requesterId, requesterContactNumber, requiredDate, compatibleTypes, donationCenterName;
    public int patientAge;
    public ImageView btnShare, btnDeleteRequest;
    public MaterialButton btnOpenRequestDetails, btnSearchDonors;


    public RequestViewHolder(@NonNull View itemView) {
        super(itemView);

        tvTitle = itemView.findViewById(R.id.tvTitle);
        btnShare = itemView.findViewById(R.id.btnShareOnFacebook);
        tvUnitsNeeded = itemView.findViewById(R.id.tvUnits);
        tvShowRequiredDate = itemView.findViewById(R.id.tvShowRequiredDate);
        tvCompatibleBloodTypes = itemView.findViewById(R.id.tvCompatibleBloodTypes);
        btnOpenRequestDetails = itemView.findViewById(R.id.btnOpenRequestDetails);
        btnSearchDonors = itemView.findViewById(R.id.btnSearchDonors);
        btnDeleteRequest = itemView.findViewById(R.id.btnDeleteRequest);
    }
}
