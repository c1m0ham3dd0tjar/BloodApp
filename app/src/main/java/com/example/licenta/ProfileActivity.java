package com.example.licenta;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class ProfileActivity extends AppCompatActivity {

    private String name, email, phone, availableForDonations, bloodType, rh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        ImageView imgViewBloodType = findViewById(R.id.imgViewBloodType);
        imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.apositive));
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPhone = findViewById(R.id.tvPhoneNumber);
        TextView tvAvailableForDonations = findViewById(R.id.tvAvailableForDonations);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String currentUserUid = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                email = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                phone = Objects.requireNonNull(snapshot.child("phone").getValue()).toString();
                availableForDonations = Objects.requireNonNull(snapshot.child("available").getValue()).toString();
                bloodType = Objects.requireNonNull(snapshot.child("bloodType").getValue()).toString();
                rh = Objects.requireNonNull(snapshot.child("rh").getValue()).toString();

                setBloodTypeImage(bloodType, rh, imgViewBloodType);

                tvName.setText(name);
                tvEmail.setText(email);
                tvPhone.setText(phone);
                if (availableForDonations.equals("true")) {
                    tvAvailableForDonations.setText(getResources().getString(R.string.available_for_donations));
                } else {
                    tvAvailableForDonations.setText(getResources().getString(R.string.not_available_for_donations));
                }

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getBaseContext(), EditProfileActivity.class);
                        intent.putExtra("currentUserName", name);
                        intent.putExtra("currentUserPhone", phone);
                        intent.putExtra("availableForDonations", availableForDonations);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadProfileInfo:onCancelled", error.toException());
            }
        };

        ref.addValueEventListener(listener);

    }

    public void setBloodTypeImage(String bloodType, String rh, ImageView imgViewBloodType) {

        switch(bloodType) {
            case "0(I)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.opositive));
                else
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.onegative));
                break;

            case "A(II)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.apositive));
                else
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.anegative));
                break;

            case "B(III)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.bpositive));
                else
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.bnegative));
                break;

            case "AB(IV)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.abpositive));
                else
                    imgViewBloodType.setImageDrawable(getResources().getDrawable(R.drawable.abnegative));
                break;
        }
    }
}