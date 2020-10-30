package com.example.licenta;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.firebase.ui.auth.AuthUI.TAG;


public class ProfileFragment extends Fragment{

    private Button btnSignOut, btnResetPassword;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View fragmentProfileView = inflater.inflate(R.layout.fragment_profile, container, false);

        btnSignOut = fragmentProfileView.findViewById(R.id.btnSignOut);
        btnResetPassword = fragmentProfileView.findViewById(R.id.btnResetPassword);
        mAuth = FirebaseAuth.getInstance();

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    Toast.makeText(getActivity(), getString(R.string.signed_out), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return fragmentProfileView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void updateUI(FirebaseUser currentUser) {

    }

    public void openResetPasswordActivity(View view) {
        startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
    }

    // Deletes user account
    public void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Profilul dumneavoastra a fost șters.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User account deleted.");
                        }
                    }
                });

    }

}

