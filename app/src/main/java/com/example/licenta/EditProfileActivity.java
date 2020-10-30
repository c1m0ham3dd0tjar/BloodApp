package com.example.licenta;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Spinner bloodTypesSpinner = findViewById(R.id.spinnerEditBloodType);
        TextInputLayout editNameTextField = findViewById(R.id.editNametTextField);
        TextInputLayout editPhoneTextField = findViewById(R.id.editPhoneTextField);
        TextInputEditText editNameTextEdit = findViewById(R.id.editNameTextEdit);
        TextInputEditText editPhoneTextEdit = findViewById(R.id.editPhoneTxtEdit);
        MaterialCheckBox checkBoxAvailable = findViewById(R.id.checkBoxAvailableEP);
        MaterialButton btnSaveProfileEdits = findViewById(R.id.btnSaveProfileEdits);
        ProgressIndicator progressIndicator = findViewById(R.id.progressIndicator);
        progressIndicator.setVisibility(View.INVISIBLE);

        // Set the layout with the old user info
        String currentUserName = getIntent().getStringExtra("currentUserName");
        editNameTextEdit.setText(currentUserName);

        String currentUserPhone = getIntent().getStringExtra("currentUserPhone");
        editPhoneTextEdit.setText(currentUserPhone);

        String availableForDonations = getIntent().getStringExtra("availableForDonations");
        assert availableForDonations != null;
        if (availableForDonations.equals("true"))
            checkBoxAvailable.setChecked(true);
        //bloodTypesSpinner.setSelection(7);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.blood_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypesSpinner.setAdapter(adapter);

        btnSaveProfileEdits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the progress indicator visible.
                progressIndicator.setVisibility(View.VISIBLE);

                // Get the new user info from the UI
                String newUserName = Objects.requireNonNull(editNameTextEdit.getText()).toString().trim();
                String newUserPhone = Objects.requireNonNull(editPhoneTextEdit.getText()).toString().trim();
                // You can't have a new blood type. This is just in case the user introduced the wrong blood type in the first place.
                String newBloodType = bloodTypesSpinner.getSelectedItem().toString();

                boolean availableForDonations2;
                availableForDonations2 = checkBoxAvailable.isChecked();

                String newRh = "";
                if (newBloodType.substring(newBloodType.length() - 1).equals("+"))
                    newRh = "Pozitiv";
                else
                    newRh = "Negativ";

                // Check if data is correct
                if (!validateData(newUserName, newUserPhone, editNameTextEdit, editPhoneTextEdit)) {
                    Toast.makeText(EditProfileActivity.this, "Datele introduse nu sunt corecte!", Toast.LENGTH_SHORT).show();
                }

                // Update the profile in the database
                else {
                    updateProfile(newUserName, newUserPhone, newBloodType, newRh, availableForDonations2);
                }
            }
        });


    }

    public void updateProfile(String newUserName, String newUserPhone, String newBloodType, String newRh, boolean isAvailable) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String currentUserUid = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ref.child("name").setValue(newUserName);
                ref.child("phone").setValue(newUserPhone);
                ref.child("bloodType").setValue(newBloodType.substring(0, newBloodType.length()-1));
                ref.child("rh").setValue(newRh);
                ref.child("available").setValue(isAvailable);

                Toast.makeText(EditProfileActivity.this, "Modificările au fost salvate cu succes.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Ceva nu a mers bine. Te rugăm să încerci din nou.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "savingProfileEditings:onCancelled", error.toException());
            }
        };

        ref.addListenerForSingleValueEvent(listener);
    }

    public boolean validateData(String name, String phoneNumber, TextInputEditText nameTextEdit, TextInputEditText phoneTextEdit) {

        boolean valid = true;

        if (isEmpty(nameTextEdit)) {
            nameTextEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (name.length() < 3) {
            nameTextEdit.setError(getString(R.string.name_too_short_error));
            valid = false;
        }
            else {
            nameTextEdit.setError(null);
        }

        if (isEmpty(phoneTextEdit)) {
            phoneTextEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (!isValidPhoneNumber(phoneNumber)) {
            phoneTextEdit.setError(getString(R.string.input_error_not_a_phone_number));
            valid = false;
        } else {
            phoneTextEdit.setError(null);
        }

        return valid;
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        boolean valid = true;
        if (phoneNumber.length() != 10)
            valid = false;
//        if (!(phoneNumber.charAt(0) == '0') && !(phoneNumber.charAt(1) == '7'))
//            valid = false;

        return valid;
    }

    // Check if input fields are completed
    public boolean isEmpty(TextInputEditText textInputEditText) {
        CharSequence str = Objects.requireNonNull(textInputEditText.getText()).toString();
        return TextUtils.isEmpty(str);
    }
}