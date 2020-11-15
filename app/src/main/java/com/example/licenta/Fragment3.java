package com.example.licenta;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta.model.Appointment;
import com.example.licenta.viewholder.AppointmentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Objects;

public class Fragment3 extends Fragment {


    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private RecyclerView recyclerViewAppointments;
    private RecyclerView.LayoutManager layoutManager;
    private String currentUserUid;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment3_layout, container, false);

        currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        recyclerViewAppointments = view.findViewById(R.id.recyclerViewAppointments);
        recyclerViewAppointments.setHasFixedSize(true);

        if (getContext() != null)
            layoutManager = new LinearLayoutManager(getContext());
        recyclerViewAppointments.setLayoutManager(layoutManager);

        displayListOfAppointments();

        return view;
    }

    public void displayListOfAppointments() {

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserId + "/Appointments/");

        FirebaseRecyclerOptions<Appointment> options = new FirebaseRecyclerOptions.Builder<Appointment>()
                .setQuery(ref, Appointment.class)
                .build();

        FirebaseRecyclerAdapter<Appointment, AppointmentViewHolder> adapter = new FirebaseRecyclerAdapter<Appointment, AppointmentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AppointmentViewHolder holder, int i, @NonNull Appointment appointment) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid + "/Appointments/"
                        + appointment.getKey());

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (Objects.equals(snapshot.child("donationConfirmed").getValue(), true)) {
                            holder.btnConfirmDonation.setVisibility(View.INVISIBLE);
                            holder.btnEditAppointment.setVisibility(View.INVISIBLE);
                            holder.imgDonationConfirmed.setVisibility(View.VISIBLE);
                        }
                        else {
                            holder.btnConfirmDonation.setVisibility(View.VISIBLE);
                            holder.btnEditAppointment.setVisibility(View.VISIBLE);
                            holder.imgDonationConfirmed.setVisibility(View.INVISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.tvPatientName.setText(appointment.getPatientName());
                holder.tvContactNumber.setText(appointment.getContactNumber());
                holder.tvDonationCenter.setText(appointment.getDonationCenter());

                // dateTimeAppointment is the date and time when you can confirm your donation, meaning (9 minutes) after the time of the appointment
                // Checking if now (current time) is after dateTimeAppointment. In that case, user can confirm his donation. Otherwise, a toast with info
                // will be displayed
                
                LocalDate localDate = LocalDate.of(appointment.getYear(), appointment.getMonth(), appointment.getDay());
                LocalTime localTime = LocalTime.of(appointment.getHour(), appointment.getMinutes());
                
                LocalTime localTime2 = LocalTime.of(appointment.getHour(), appointment.getMinutes());
                LocalDateTime dateTimeAppointment = LocalDateTime.of(localDate, localTime2);

                holder.tvDate.setText(String.format("%s %s", localDate.toString(), localTime.toString()));

                holder.btnConfirmDonation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocalDateTime now = LocalDateTime.now();
                        
                        if (now.isAfter(dateTimeAppointment)) {

                            Intent intent = new Intent(getContext(), ScannerActivity.class);
                            intent.putExtra("appointmentKey", appointment.getKey());
                            startActivity(intent);
                        } 
                        else {
                            Toast.makeText(getContext(), String.format("%s %s %s", getResources().getString(R.string.string_confirmation_error), localDate.toString(), localTime.toString()), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                
                holder.btnDeleteAppointment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getActivity());
                        dialog.setTitle(getResources().getString(R.string.string_delete_appointment))
                                .setMessage(getResources().getString(R.string.string_are_you_sure))
                                .setPositiveButton(getResources().getString(R.string.confirmation), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserId +
                                                "/Appointments/" + appointment.getKey());
                                        ref.removeValue().addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), getResources().getString(R.string.string_confirmed_data), Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), getResources().getString(R.string.string_error), Toast.LENGTH_SHORT).show();
                                            Log.e("DeleteAppointmentFail", e.toString());
                                        });
                                    }
                                })
                               // .setNegativeButton(getResources().getString(R.string.string_cancel), )
                                .show();
                    }
                });

                holder.btnEditAppointment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        updateAppointment(year, month + 1, dayOfMonth, hourOfDay, minute, appointment.getKey());
                                    }
                                };
                                new TimePickerDialog(requireContext(), timeSetListener,
                                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                            }
                        };

                        new DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });




            }

            @NonNull
            @Override
            public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_listitem_appointments, parent, false);

                return new AppointmentViewHolder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerViewAppointments.setAdapter(adapter);
    }

    public void updateAppointment(int year, int month, int dayOfMonth, int hourOfDay, int minute, String key) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid + "/Appointments/" + key);
        ref.child("year").setValue(year);
        ref.child("month").setValue(month);
        ref.child("day").setValue(dayOfMonth);
        ref.child("hour").setValue(hourOfDay);
        ref.child("minutes").setValue(minute).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), getResources().getString(R.string.string_confirmed_data), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), getResources().getString(R.string.string_error) , Toast.LENGTH_SHORT).show();
            Log.e("updateAppointmentFail:", e.toString());
        });

    }


}
