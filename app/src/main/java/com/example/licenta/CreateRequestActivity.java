package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.licenta.model.Request;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Objects;

public class CreateRequestActivity extends AppCompatActivity {

    public static final String BLOOD_TYPE_0_MINUS = "0(I)-";
    public static final String BLOOD_TYPE_0_PLUS = "0(I)+";
    public static final String BLOOD_TYPE_A_MINUS = "A(II)-";
    public static final String BLOOD_TYPE_A_PLUS = "A(II)+";
    public static final String BLOOD_TYPE_B_MINUS = "B(III)-";
    public static final String BLOOD_TYPE_B_PLUS = "B(III)+";
    public static final String BLOOD_TYPE_AB_MINUS = "AB(IV)-";
    public static final String BLOOD_TYPE_AB_PLUS = "AB(IV)+";

    private static final String TAG = "";
    private Spinner spinnerBloodType, spinnerLocations;
    private ElegantNumberButton elegantButton;
    private TextView tvBloodType, tvUnitsNeeded, tvPatientLocation, tvRequiredDate;
    private TextInputEditText patientNameTxtEdit, contactPhoneTxtEdit, patientAgeTxtEdit;
    private TextInputLayout patientNameTxtField, contactPhoneTxtField;
    private androidx.appcompat.widget.Toolbar toolbar;
    private ProgressBar progressBarCreateRequest;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private ImageView btnSetRequiredDate;
    private MaterialCheckBox checkBoxMan, checkBoxWoman;
    private boolean isDateSet = false;
    private LocalDate requiredUptoDate;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        MaterialButton btnSaveRequest = findViewById(R.id.btnSaveRequest);

        tvBloodType = findViewById(R.id.tvBloodType);
        tvUnitsNeeded = findViewById(R.id.tvUnitsNeeded);
        tvPatientLocation = findViewById(R.id.tvPatientLocation);
        tvRequiredDate = findViewById(R.id.tvRequiredDate);

        patientNameTxtField = findViewById(R.id.txtFieldPatientName);
        contactPhoneTxtField = findViewById(R.id.contactPhoneTxtField);

        patientNameTxtEdit = findViewById(R.id.txtEditPatientName);
        contactPhoneTxtEdit = findViewById(R.id.contactPhoneTxtEdit);
        patientAgeTxtEdit = findViewById(R.id.txtEditPatientAge);

        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        spinnerLocations = findViewById(R.id.spinnerLocations);
        elegantButton = findViewById(R.id.elegantButton);
        elegantButton.setNumber("1");
        elegantButton.setRange(1, 100);
        btnSetRequiredDate = findViewById(R.id.btnSetRequiredDate);

        toolbar = findViewById(R.id.toolbarCreateRequest);
        progressBarCreateRequest = findViewById(R.id.progressBarCreateRequest);

        progressBarCreateRequest.setVisibility(View.INVISIBLE);
        checkBoxMan = findViewById(R.id.checkboxMan);
        checkBoxWoman = findViewById(R.id.checkBoxWoman);

        // Checkbox functionality
        checkBoxMan.setChecked(true);
        checkBoxMan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxWoman.setChecked(!checkBoxMan.isChecked());
            }
        });
        checkBoxWoman.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxMan.setChecked(!checkBoxWoman.isChecked());
            }
        });


        // For blood type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(adapter);

        // For locations spinner
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.donation_centers_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocations.setAdapter(adapter2);

        // Set toolbar icon and navigation
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getColor(R.color.color_white));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        // Set on click listener for text field, open date picker
        btnSetRequiredDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                DatePickerDialog dialog = new DatePickerDialog(CreateRequestActivity.this);
                dialog.show();
                dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        requiredUptoDate = LocalDate.of(year, month + 1, dayOfMonth);
                        tvRequiredDate.setText(requiredUptoDate.toString());
                        isDateSet = true;
                    }
                });


            }
        });

        btnSaveRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get data from UI
                String bloodType = spinnerBloodType.getSelectedItem().toString();
                int bloodUnitsNeeded = Integer.parseInt(elegantButton.getNumber());
                String location = spinnerLocations.getSelectedItem().toString();
                String patientName = Objects.requireNonNull(patientNameTxtEdit.getText()).toString().trim();
                String contactPhone = Objects.requireNonNull(contactPhoneTxtEdit.getText()).toString().trim();
                String requesterId = currentUser.getUid();
                int patientAge = Integer.parseInt(patientAgeTxtEdit.getText().toString());
                String patientGender = "";
                if (checkBoxMan.isChecked())
                    patientGender = "Man";
                else if (checkBoxWoman.isChecked())
                    patientGender = "Woman";

                if (!validateData(patientName, contactPhone, patientAge, requiredUptoDate)) {
                    Toast.makeText(CreateRequestActivity.this, getString(R.string.incorrect_data), Toast.LENGTH_SHORT).show();
                }
                else {
                    createRequest(bloodType, bloodUnitsNeeded, location, patientName, patientAge, patientGender, contactPhone, requesterId);
                }
            }
        });
    }

    public void createRequest(String bloodType, int bloodUnitsNeeded, String location, String patientName,
                              int patientAge, String patientGender, String contactPhone, String requesterId) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CreateRequestActivity.this);

        builder.setTitle(R.string.alert_dialog_title_create_request);
        builder.setNegativeButton(R.string.alert_dialog_create_request_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(CreateRequestActivity.this, MainActivity.class));
            }
        });

        builder.setPositiveButton(R.string.alert_dialog_create_request_positive_button, (dialog, which) -> {
            progressBarCreateRequest.setVisibility(View.VISIBLE);

            mDatabase = FirebaseDatabase.getInstance().getReference("Requests");

            List<String> compatibleBloodTypes = new ArrayList<>(getCompatibleBloodTypes(bloodType));

            Request request = new Request(
                    bloodType,
                    bloodUnitsNeeded,
                    location,
                    patientName,
                    patientAge,
                    patientGender,
                    contactPhone,
                    compatibleBloodTypes,
                    requiredUptoDate.toString(),
                    requesterId
            );

            String key = FirebaseDatabase.getInstance().getReference("Requests").push().getKey();
            if (currentUser != null) {
                // User is signed in
                assert key != null;
                mDatabase.child(key).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createRequest + save it to database:success");
                            Toast.makeText(CreateRequestActivity.this, "Solicitarea a fost salvată cu succes!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateRequestActivity.this, MainActivity.class));
                        } else {
                            Log.w(TAG, "createRequestSaveToDatabase:failure", task.getException());
                            Toast.makeText(CreateRequestActivity.this, "Ceva nu a mers bine. Te rugăm să încerci din nou.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                progressBarCreateRequest.setVisibility(View.INVISIBLE);

            }
        });
        builder.setNeutralButton("Inapoi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        if (patientNameTxtField.getError() == null) {
            builder.show();
        }
    }

    public boolean validateData(String patientName, String contactNumber, int patientAge, LocalDate requiredUptoDate) {
        boolean valid = true;

        if (isEmpty(patientNameTxtEdit)) {
            patientNameTxtEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (!isValidName(patientName)) {
            patientNameTxtEdit.setError(getString(R.string.input_error_not_a_valid_name));
            valid = false;
        } else {
            patientNameTxtEdit.setError(null);
        }

        if (isEmpty(contactPhoneTxtEdit)) {
            contactPhoneTxtEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (!isValidPhoneNumber(contactNumber)) {
            contactPhoneTxtEdit.setError(getString(R.string.input_error_not_a_phone_number));
            valid = false;
        } else {
            contactPhoneTxtEdit.setError(null);
        }

        if (isEmpty(patientAgeTxtEdit)) {
            patientAgeTxtEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (!isValidAge(patientAge)){
            patientAgeTxtEdit.setError(getString(R.string.input_error_not_a_phone_number));
            valid = false;
        } else {
            patientAgeTxtEdit.setError(null);
        }

        if (!isDateSet)
            valid = false;

        return valid;
    }

    // Check if phone number field is completed with a 10 digit number.
    public boolean isValidPhoneNumber(String phoneNumber) {
        boolean valid = true;
        if (phoneNumber.length() > 12 || phoneNumber.length() < 10)
            valid = false;
//        if (!(phoneNumber.charAt(0) == '0') && !(phoneNumber.charAt(1) == '7'))
//            valid = false;

        return valid;
    }

    public boolean isValidAge(int age) {
        boolean valid = true;

        if (age > 110)
            valid = false;
        return valid;
    }

    // Check if input fields are completed.
    public boolean isEmpty(TextInputEditText textInputEditText) {
        CharSequence str = Objects.requireNonNull(textInputEditText.getText()).toString();
        return TextUtils.isEmpty(str);
    }

    // Check if name contains only letters
    public boolean isValidName(String name) {
        boolean valid = true;
        if (!Pattern.matches("[ a-zA-Z]+", name))
            valid = false;

        return valid;
    }

    public List<String> getCompatibleBloodTypes(String bloodType) {

        List<String> compatibleBloodTypes = new ArrayList<>();

        switch(bloodType) {
            case BLOOD_TYPE_0_PLUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_PLUS);
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                break;

            case BLOOD_TYPE_0_MINUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                break;

            case BLOOD_TYPE_A_PLUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_0_PLUS);
                compatibleBloodTypes.add(BLOOD_TYPE_A_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_A_PLUS);
                break;

            case BLOOD_TYPE_A_MINUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_A_MINUS);
                break;

            case BLOOD_TYPE_B_PLUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_0_PLUS);
                compatibleBloodTypes.add(BLOOD_TYPE_B_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_B_PLUS);
                break;

            case BLOOD_TYPE_B_MINUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_B_MINUS);
                break;

            case BLOOD_TYPE_AB_PLUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_0_PLUS);
                compatibleBloodTypes.add(BLOOD_TYPE_A_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_A_PLUS);
                compatibleBloodTypes.add(BLOOD_TYPE_B_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_B_PLUS);
                compatibleBloodTypes.add(BLOOD_TYPE_AB_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_AB_PLUS);
                break;

            case BLOOD_TYPE_AB_MINUS:
                compatibleBloodTypes.add(BLOOD_TYPE_0_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_A_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_B_MINUS);
                compatibleBloodTypes.add(BLOOD_TYPE_AB_MINUS);
                break;

            default:
        }

        return compatibleBloodTypes;
    }
}
