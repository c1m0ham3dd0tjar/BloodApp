package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.licenta.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.ParseException;
import com.parse.SignUpCallback;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = " ";
    private com.google.android.material.textfield.TextInputEditText emailSignupTextEdit,
            passSignupTextEdit, confirmPassTextEdit, nameTextEdit, phoneTextEdit;
    private com.google.android.material.checkbox.MaterialCheckBox checkBoxAvailable, checkBoxUnderage;
    private RadioGroup rgBloodType, rgRH;
    private androidx.appcompat.widget.Toolbar toolbar;
    private MaterialButton btnSignup;
    private ScrollView scrollView;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        // Assignments
        emailSignupTextEdit = findViewById(R.id.emailSignupTextEdit);
        passSignupTextEdit = findViewById(R.id.passSignupTextEdit);
        confirmPassTextEdit = findViewById(R.id.confirmPassTextEdit);
        nameTextEdit = findViewById(R.id.nameTxtEdit);
        phoneTextEdit = findViewById(R.id.phoneTxtEdit);

        checkBoxAvailable = findViewById(R.id.checkBoxAvailable);
        checkBoxUnderage = findViewById(R.id.checkBoxUnderage);

        rgBloodType = findViewById(R.id.radioBloodType);
        rgRH = findViewById(R.id.radioRH);

        toolbar = findViewById(R.id.toolbarSignupActivity);
        btnSignup = findViewById(R.id.btnSignUp);
        scrollView = findViewById(R.id.scrollView);

        // Set toolbar navigation
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        toolbar.setTitleTextColor(0xFFFFFFFF);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        // Hide keyboard when radio groups items selected
        rgBloodType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                hideKeyboard(SignupActivity.this);
            }
        });

        rgRH.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                hideKeyboard(SignupActivity.this);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                String email = Objects.requireNonNull(emailSignupTextEdit.getText()).toString();
                String password = Objects.requireNonNull(passSignupTextEdit.getText()).toString();
                String confirmedPassword = Objects.requireNonNull(confirmPassTextEdit.getText()).toString();
                String name = Objects.requireNonNull(nameTextEdit.getText()).toString();
                String phoneNumber = Objects.requireNonNull(phoneTextEdit.getText()).toString();

                // Get bloodtype from radiogroup
                int bloodTypeCheckedId = rgBloodType.getCheckedRadioButtonId();
                View radioButton = rgBloodType.findViewById(bloodTypeCheckedId);
                int selectedBloodTypeIndex = rgBloodType.indexOfChild(radioButton);
                MaterialRadioButton rb = (MaterialRadioButton) rgBloodType.getChildAt(selectedBloodTypeIndex);

                // Get RH from radiogroup
                int RHCheckedId = rgRH.getCheckedRadioButtonId();
                View radioButton2 = rgRH.findViewById(RHCheckedId);
                int selectedRHIndex = rgRH.indexOfChild(radioButton2);
                MaterialRadioButton rb2 = (MaterialRadioButton) rgRH.getChildAt(selectedRHIndex);

                String bloodType = rb.getText().toString();
                String RH = rb2.getText().toString();

                Boolean isAvailable = checkBoxAvailable.isChecked();
                Boolean isNotUnderAge = checkBoxUnderage.isChecked();

                if (!validateData(email, password, confirmedPassword, phoneNumber)) {
                    Toast.makeText(SignupActivity.this, "Not good!", Toast.LENGTH_SHORT).show();
                    scrollView.smoothScrollTo(emailSignupTextEdit.getScrollX(), emailSignupTextEdit.getScrollY());
                }
                else {
                    createAccount(email, password, name, phoneNumber,bloodType, RH, isAvailable, isNotUnderAge);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createAccount(String email, String password, String name, String phone,
                              String bloodType, String RH, Boolean isAvailable, Boolean isNotUnderAge) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(
                                    email,
                                    name,
                                    phone,
                                    bloodType,
                                    RH,
                                    isAvailable,
                                    isNotUnderAge
                            );

                            FirebaseDatabase.getInstance().getReference("User")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "createUserWithEmail + custom fields:success");
                                    Toast.makeText(SignupActivity.this, "Registration complete!", Toast.LENGTH_SHORT).show();
                                    //startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                }
                            });
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public boolean validateData(String email, String password, String confirmedPassword, String phoneNumber) {

        boolean valid = true;

        if (isEmpty(emailSignupTextEdit)) {
            emailSignupTextEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (!isEmailValid(email)) {
            emailSignupTextEdit.setError(getString(R.string.input_error_wrong_email));
            valid = false;
        } else {
            emailSignupTextEdit.setError(null);
        }

        if (isEmpty(passSignupTextEdit)) {
            passSignupTextEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (!isPasswordValid(password)) {
            passSignupTextEdit.setError(getString((R.string.input_error_wrong_password)));
            valid = false;
        } else {
            passSignupTextEdit.setError(null);
        }

        if (isEmpty(confirmPassTextEdit)) {
            confirmPassTextEdit.setError(getString(R.string.input_error));
            valid = false;
        } else if (!password.equals(confirmedPassword)) {
            confirmPassTextEdit.setError(getString(R.string.input_error_mismatched_password));
            valid = false;
        } else {
            confirmPassTextEdit.setError(null);
        }

        if (isEmpty(nameTextEdit)) {
            nameTextEdit.setError(getString(R.string.input_error));
            valid = false;
        } else {
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

        if (!checkBoxUnderage.isChecked()) {
            checkBoxUnderage.setError(getString(R.string.not_checked));
            valid = false;
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

    // Check if email is a valid format
    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Check is password is at least 8 characters long
    public boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}


