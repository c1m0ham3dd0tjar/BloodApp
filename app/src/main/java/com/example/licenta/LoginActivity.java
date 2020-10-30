package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private static String TAG = "" ;
    private com.google.android.material.textfield.TextInputEditText emailTxtEdit, passwordTxtEdit;
    private MaterialButton btnLogin, btnGoToSignup, btnGoToForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailTxtEdit = findViewById(R.id.emailTextEdit);
        passwordTxtEdit = findViewById(R.id.passTextEdit);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToSignup = findViewById(R.id.btnGoToSignup);
        btnGoToForgotPassword = findViewById(R.id.btnGoToForgotPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(emailTxtEdit.getText()).toString();
                String password = Objects.requireNonNull(passwordTxtEdit.getText()).toString();

                if (!validateData(email, password)) {
                    // Do something
                }
                else {
                    login(email, password);
                }
            }
        });

        btnGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnGoToForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

    }

    // If the user is logged in, skip the LoginActivity and go to the MainActivity.
    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        // FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUi(currentUser);

        // Start listening for changes
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mAuth.removeAuthStateListener(authStateListener);
    }

    public void login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Authentication succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            // FirebaseUser user = mAuth.getCurrentUser();
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.authentification_failed),
                                    Toast.LENGTH_LONG).show();
                            // updateUI(null);
                        }
                    }
                });
    }

    public boolean validateData(String email, String password) {
        boolean valid = true;

        if (isEmpty(emailTxtEdit)) {
            valid = false;
            emailTxtEdit.setError(getString(R.string.input_error));
        }
        else {
            emailTxtEdit.setError(null);
        }

        if (isEmpty(passwordTxtEdit)) {
            valid = false;
            passwordTxtEdit.setError(getString(R.string.input_error));
        }

        return valid;
    }

    public boolean isEmpty(TextInputEditText textInputEditText) {
        CharSequence str = Objects.requireNonNull(textInputEditText.getText()).toString();
        return TextUtils.isEmpty(str);
    }

    public void openHomeActivity(View view) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void openForgotPasswordActivity(View view) {

        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}
