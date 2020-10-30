package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licenta.model.Appointment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CreateAppointment extends AppCompatActivity {

    private int reminderMinutes;
    private int reminderHours;

    LocalDate appointmentDate;
    LocalTime appointmentTime;

    int appointmentsCounter;
    FirebaseUser currentUser;

    ProgressBar progressBar;

//    String patientName = "";
//    String requesterPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);

        // Current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar = findViewById(R.id.progressSaveAppointment);

        // Animations
        Animation pushLeftInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in_animation);
        Animation pushLeftOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_out_animation);
        Animation pushRightInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_in_animation);

        // Assignments for choose donation center card view
        MaterialCardView cardViewChooseDonationCenter = findViewById(R.id.cardViewChooseDonationCenter);
        TextView tvDonationCenterName = findViewById(R.id.tvDonationCenterName);
        TextView tvDonationCenterAddress = findViewById(R.id.tvDonationCenterAdress);
        AppCompatImageButton btnNext = findViewById(R.id.btnNext);

        AppCompatSpinner spinnerDonationCenters = findViewById(R.id.spinnerDonationCenters);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
                R.array.donation_centers_array, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDonationCenters.setAdapter(adapter3);

        // Assignments for choose date card view
        MaterialCardView cardViewChooseDate = findViewById(R.id.cardViewChooseDate);
        DatePicker datePicker = findViewById(R.id.datePicker);
        AppCompatImageButton btnNext2 = findViewById(R.id.btnNext2);

        AppCompatSpinner spinnerHours = findViewById(R.id.spinnerHours);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hours_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHours.setAdapter(adapter);

        // Asignments for set reminder card view
        MaterialCardView cardViewReminder = findViewById(R.id.cardViewReminder);
        TextView tvReminderInfo = findViewById(R.id.tvReminderInfo);
        AppCompatImageButton btnNext3 = findViewById(R.id.btnNext3);

        AppCompatSpinner spinnerReminders = findViewById(R.id.spinnerReminders);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.reminders_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminders.setAdapter(adapter2);


        // Assignments for final card view
        MaterialCardView cardViewFinalInfo = findViewById(R.id.cardViewFinalInfo);
        TextView tvDonationCenterNameFinal = findViewById(R.id.tvDonationCenterNameFinal);
        TextView tvDateFinal = findViewById(R.id.tvDateFinal);
        TextView tvHourFinal = findViewById(R.id.tvHourFinal);
        TextView tvReminderFinal = findViewById(R.id.tvReminderFinal);

        AppCompatImageButton btnEditDonationCenter = findViewById(R.id.btnEditDonationCenter);
        AppCompatImageButton btnEditDate = findViewById(R.id.btnEditDate);
        AppCompatImageButton btnEditHour = findViewById(R.id.btnEditHour);
        AppCompatImageButton btnEditReminder = findViewById(R.id.btnEditReminder);

        MaterialButton btnSaveAppointment = findViewById(R.id.btnSaveAppointment2);

        // Set toolbar and toolbar navigation
        MaterialToolbar toolbarCreateAppointment = findViewById(R.id.toolbarCreateAppointment);
        setSupportActionBar(toolbarCreateAppointment);

        toolbarCreateAppointment.setNavigationIcon(R.drawable.ic_action_back);
        toolbarCreateAppointment.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateAppointment.this, MainActivity.class));
            }
        });

        spinnerDonationCenters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = spinnerDonationCenters.getSelectedItem().toString();
                String str2 = str.substring(31);
                String formatedStr = "CTS " + str2;
                tvDonationCenterName.setText(formatedStr);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DonationCenter");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            if (data.child("name").getValue().equals(str)) {
                                tvDonationCenterAddress.setText(data.child("adress").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                tvDonationCenterNameFinal.setText(formatedStr);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Implements card views behaviour. When data is completed in a card view, and the next button is pressed, the current
        // cardView will be hidden, and the next one will appear, with an animation.

        btnNext.setOnClickListener(v -> {
            cardViewChooseDonationCenter.setVisibility(View.GONE);
            cardViewReminder.setVisibility(View.GONE);
            cardViewFinalInfo.setVisibility(View.GONE);

            cardViewChooseDate.setVisibility(View.VISIBLE);
            cardViewChooseDate.startAnimation(pushLeftInAnimation);



        });
        btnNext2.setOnClickListener(v -> {

            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();
            appointmentDate = LocalDate.of(year, month, day);

            String timeString = spinnerHours.getSelectedItem().toString();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
            appointmentTime = LocalTime.parse(timeString, formatter);

            tvDateFinal.setText(appointmentDate.toString());
            tvHourFinal.setText(appointmentTime.toString());

            cardViewChooseDonationCenter.setVisibility(View.GONE);
            cardViewChooseDate.setVisibility(View.GONE);
            cardViewFinalInfo.setVisibility(View.GONE);

            cardViewReminder.setVisibility(View.VISIBLE);
            cardViewReminder.startAnimation(pushLeftInAnimation);
        });
        btnNext3.setOnClickListener(v -> {

            String reminderString = spinnerReminders.getSelectedItem().toString();
            tvReminderFinal.setText(reminderString);

            if (reminderString.length() == 9) {
                reminderHours = 0;
                reminderMinutes = Integer.parseInt(reminderString.substring(0, 2));
            } else if (reminderString.length() == 5) {
                reminderMinutes = 0;
                reminderHours = Integer.parseInt(reminderString.substring(0, 1));
            } else {
                reminderMinutes = 0;
                reminderHours = Integer.parseInt(reminderString.substring(0, 2));
            }

            cardViewChooseDonationCenter.setVisibility(View.GONE);
            cardViewReminder.setVisibility(View.GONE);
            cardViewChooseDate.setVisibility(View.GONE);

            cardViewFinalInfo.setVisibility(View.VISIBLE);
            cardViewFinalInfo.startAnimation(pushLeftInAnimation);
        });

        // Edit buttons listeners
        btnEditDonationCenter.setOnClickListener(v -> {
            cardViewFinalInfo.setVisibility(View.GONE);
            cardViewReminder.setVisibility(View.GONE);
            cardViewChooseDate.setVisibility(View.GONE);

            cardViewChooseDonationCenter.setVisibility(View.VISIBLE);
            cardViewChooseDonationCenter.setAnimation(pushRightInAnimation);
        });
        btnEditDate.setOnClickListener(v -> {
            cardViewFinalInfo.setVisibility(View.GONE);
            cardViewReminder.setVisibility(View.GONE);
            cardViewChooseDonationCenter.setVisibility(View.GONE);

            cardViewChooseDate.setVisibility(View.VISIBLE);
            cardViewChooseDate.setAnimation(pushRightInAnimation);
        });
        btnEditHour.setOnClickListener(v -> {
            cardViewFinalInfo.setVisibility(View.GONE);
            cardViewReminder.setVisibility(View.GONE);
            cardViewChooseDonationCenter.setVisibility(View.GONE);

            cardViewChooseDate.setVisibility(View.VISIBLE);
            cardViewChooseDate.setAnimation(pushRightInAnimation);
        });
        btnEditReminder.setOnClickListener(v -> {
            cardViewFinalInfo.setVisibility(View.GONE);
            cardViewChooseDate.setVisibility(View.GONE);
            cardViewChooseDonationCenter.setVisibility(View.GONE);

            cardViewReminder.setVisibility(View.VISIBLE);
            cardViewReminder.setAnimation(pushRightInAnimation);
        });

        // Save appointment button, on click listener
        btnSaveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String donationCenterName = spinnerDonationCenters.getSelectedItem().toString();
                String reminder = tvReminderFinal.getText().toString();

                String patientName = "";
                String requesterContactPhone = "";

                saveAppointment(appointmentDate, appointmentTime, donationCenterName, patientName, requesterContactPhone, reminder);
            }
        });

    }

    public void saveAppointment(LocalDate localDate, LocalTime localTime, String donationCenterName, String patientName, String requesterContactPhone, String reminder) {

        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        String currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String key = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid + "Appointments").push().getKey();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid + "/Appointments/" + key);


        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid + "/Appointments/");

        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (Objects.requireNonNull(data.child("donationConfirmed").getValue()).toString().equals("false")) {
                        appointmentsCounter++;
                    }
                }

                if (appointmentsCounter > 0) {
                    Toast.makeText(CreateAppointment.this, getResources().getString(R.string.string_multiple_appointments_error), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    Appointment appointment = new Appointment(
                            localDateTime.getDayOfMonth(),
                            localDateTime.getMonthValue(),
                            localDateTime.getYear(),
                            localDateTime.getHour(),
                            localDateTime.getMinute(),
                            donationCenterName,
                            patientName,
                            requesterContactPhone,
                            key,
                            false,
                            reminder);


                    if (currentUser != null) {
                        ref.setValue(appointment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateAppointment.this, getResources().getString(R.string.appointment), Toast.LENGTH_LONG).show();
                                startActivity(new Intent(CreateAppointment.this, MainActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateAppointment.this, getResources().getString(R.string.appointment_failure), Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateAppointment.this, getResources().getString(R.string.string_appointment_error), Toast.LENGTH_SHORT).show();
            }
        });

    }
}