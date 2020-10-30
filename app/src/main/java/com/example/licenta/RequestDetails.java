package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licenta.model.Appointment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class RequestDetails extends AppCompatActivity {

    private TextView tvTitleRequestDetails, tvUnitsNeeded, tvLocation, tvRequesterPhoneNumber,
            tvPatientName, tvPatientAge, tvCompatibleGroups, tvPhoneNumbers, tvDonationCenterAdress, tvRequiredUptoDate,
            tvDonationCenter, tvAppointmentDate, tvAppointmentHour, tvYouChose;

    private androidx.appcompat.widget.Toolbar toolbar;
    private Button btnShowDatePicker, btnPickDate, btnSaveAppointment;
    private MaterialButton btnPickHour;
    private TimePickerDialog timePickerDialog2;

    private de.hdodenhof.circleimageview.CircleImageView imgViewBlood;

    private String centerPhoneNumber1 = "", centerPhoneNumber2 = "", donationCenterAdress, requiredDate,
            compatibleTypes, patientName, donationCenter, requesterContactPhone;
    private int counter = 0;

    FirebaseUser currentUser;
    String currentUserUid;

    public LocalDate appointmentDate;
    public LocalTime appointmentHour;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        currentUserUid = currentUser.getUid();

        btnSaveAppointment = findViewById(R.id.btnSaveAppointment);
        btnPickDate = findViewById(R.id.btnShowDateAndTimePicker);
        btnPickHour = findViewById(R.id.btnPickHour);
        toolbar = findViewById(R.id.toolBarRequestDetails);

        tvUnitsNeeded = findViewById(R.id.tvUnitsNeededRequestDetails);
        tvRequesterPhoneNumber = findViewById(R.id.tvRequesterPhoneNumberRD);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientAge = findViewById(R.id.tvPatientAge);
        tvCompatibleGroups = findViewById(R.id.tvCompatibleGroups);
        tvPhoneNumbers = findViewById(R.id.tvPhoneNumbersRD);
        tvDonationCenterAdress = findViewById(R.id.tvDonationCenterAdressRD);
        tvRequiredUptoDate = findViewById(R.id.tvRequiredUptoDate2);
        tvDonationCenter = findViewById(R.id.tvDonationCenterRD);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentHour = findViewById(R.id.tvAppointmentHour);
        tvYouChose = findViewById(R.id.tvYouChose);

        imgViewBlood = findViewById(R.id.imgViewBlood);

        Intent intent = getIntent();
        patientName = Objects.requireNonNull(intent.getExtras()).getString("Patient name");
        donationCenter = intent.getExtras().getString("donationCenterName");
        String bloodtype = intent.getExtras().getString("Bloodtype");
        String unitsNeeded = intent.getExtras().getString("Units needed");
        String requesterId = intent.getExtras().getString("Requester id");
        int patientAge = intent.getExtras().getInt("patientAge");
        requesterContactPhone = intent.getExtras().getString("ContactPhone");
        requiredDate = intent.getExtras().getString("requiredDate");
        compatibleTypes = intent.getExtras().getString("compatibleTypes");

        // Set the image wich represents the patient's blood type.
        assert bloodtype != null;
        String rh;
        if (bloodtype.substring(bloodtype.length() - 1).equals("+"))
            rh = "Pozitiv";
        else
            rh = "Negativ";
        String bloodType = bloodtype.substring(0, bloodtype.length() - 1);
        setBloodTypeImage(bloodType, rh, imgViewBlood);

        tvPatientName.setText(patientName);
        tvPatientAge.setText(patientAge + " " + getString(R.string.years));
        tvUnitsNeeded.setText(unitsNeeded + " " + getString(R.string.units));
        tvRequesterPhoneNumber.setText(requesterContactPhone);
        tvRequiredUptoDate.setText(requiredDate.replace("[]", " "));

        StringTokenizer tokenizer = new StringTokenizer(compatibleTypes, "[],");
        tvCompatibleGroups.setText("");

        while (tokenizer.hasMoreTokens()) {
            tvCompatibleGroups.append(tokenizer.nextToken() + "  ");
        }

        // Get the donation center phone numbers and show them into the textView;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DonationCenter");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot data : snapshot.getChildren()) {
                    if (Objects.requireNonNull(data.child("name").getValue()).toString().equals(donationCenter)) {
                        centerPhoneNumber1 = Objects.requireNonNull(data.child("phone1").getValue()).toString();
                        centerPhoneNumber2 = Objects.requireNonNull(data.child("phone2").getValue()).toString();
                        donationCenterAdress = Objects.requireNonNull(data.child("adress").getValue()).toString();

                        Log.d("address", donationCenterAdress);
                        tvDonationCenterAdress.setText(donationCenterAdress);
                        tvPhoneNumbers.setText(centerPhoneNumber1 + " / " + centerPhoneNumber2);

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadDonationCenterPhoneNumber:onCancelled", error.toException());
            }
        };

        ref.addValueEventListener(listener);

        tvDonationCenter.setText(donationCenter);

        // Set navigation on toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));


        // Set onClickListener for dateTimeDialogPicker
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(RequestDetails.this);
                dialog.show();
                dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        appointmentDate = LocalDate.of(year, month + 1, dayOfMonth);
                        tvAppointmentDate.setText(appointmentDate.toString());
                        btnPickHour.setEnabled(true);
                        tvYouChose.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        btnPickHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(RequestDetails.this);
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                final View myView = inflater.inflate(R.layout.pick_hour_view, null);

                AppCompatSpinner spinnerHours = myView.findViewById(R.id.spinnerChoseHour);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(RequestDetails.this,
                        R.array.hours_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerHours.setAdapter(adapter);

                dialog.setView(myView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String hour = spinnerHours.getSelectedItem().toString();
                                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
                                appointmentHour = LocalTime.parse(hour, formatter);
                                tvAppointmentHour.setText(hour);
                                btnSaveAppointment.setEnabled(true);
                            }
                        })
                        .show();
            }
        });

        btnSaveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAppointment(appointmentDate, appointmentHour);
            }
        });
    }


    public void saveAppointment(LocalDate localDate, LocalTime localTime) {

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
                        counter++;
                    }
                }

                if (counter > 0)
                    Toast.makeText(RequestDetails.this, getResources().getString(R.string.string_multiple_appointments_error), Toast.LENGTH_SHORT).show();
                else {
                    Appointment appointment = new Appointment(
                            localDateTime.getDayOfMonth(),
                            localDateTime.getMonthValue(),
                            localDateTime.getYear(),
                            localDateTime.getHour(),
                            localDateTime.getMinute(),
                            donationCenter,
                            patientName,
                            requesterContactPhone,
                            key,
                            false,
                            "");


                    if (currentUser != null) {
                        ref.setValue(appointment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(RequestDetails.this, getResources().getString(R.string.appointment), Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RequestDetails.this, MainActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RequestDetails.this, getResources().getString(R.string.appointment_failure), Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RequestDetails.this, getResources().getString(R.string.string_appointment_error), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void setBloodTypeImage(String bloodType, String rh, CircleImageView imgViewBlood) {

        switch(bloodType) {
            case "0(I)":
                if (rh.equals("Pozitiv"))
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.opositive));
                else
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.onegative));
                break;

            case "A(II)":
                if (rh.equals("Pozitiv"))
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.apositive));
                else
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.anegative));
                break;

            case "B(III)":
                if (rh.equals("Pozitiv"))
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.bpositive));
                else
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.bnegative));
                break;

            case "AB(IV)":
                if (rh.equals("Pozitiv"))
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.abpositive));
                else
                    imgViewBlood.setImageDrawable(getResources().getDrawable(R.drawable.abnegative));
                break;
        }
    }
}
