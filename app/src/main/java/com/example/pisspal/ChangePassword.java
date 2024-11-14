package com.example.pisspal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    FirebaseAuth authProfile;
    EditText editTextPwd, editTextNewPwd, editTextConfirmPwd;
    TextView verify;
    Button but_authenticate, but_changePwd;
    String userCurPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize views
        editTextPwd = findViewById(R.id.input_password);
        editTextNewPwd = findViewById(R.id.new_password);
        editTextConfirmPwd = findViewById(R.id.confirm_new_password);
        verify = findViewById(R.id.verification);
        but_authenticate = findViewById(R.id.check_user);
        but_changePwd = findViewById(R.id.update_password);

        // Disable new password fields initially
        editTextNewPwd.setEnabled(false);
        editTextConfirmPwd.setEnabled(false);
        but_changePwd.setEnabled(false);

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(getApplicationContext(), "User's details not available", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if user is not authenticated
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        but_authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userCurPwd = editTextPwd.getText().toString();

                if (TextUtils.isEmpty(userCurPwd)) {
                    Toast.makeText(getApplicationContext(), "Password is needed", Toast.LENGTH_LONG).show();
                    editTextPwd.setError("Please enter your current password");
                    editTextPwd.requestFocus();
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userCurPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                editTextPwd.setEnabled(false);
                                editTextNewPwd.setEnabled(true);
                                editTextConfirmPwd.setEnabled(true);

                                but_authenticate.setEnabled(false);
                                but_changePwd.setEnabled(true);

                                verify.setText("You are verified. You can change the password now.");
                                Toast.makeText(getApplicationContext(), "Password has been verified. Change password now.", Toast.LENGTH_SHORT).show();

                                but_changePwd.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.blue));

                                but_changePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Password verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userNewPwd = editTextNewPwd.getText().toString();
        String userPwdConfNew = editTextConfirmPwd.getText().toString();

        if (TextUtils.isEmpty(userNewPwd)) {
            Toast.makeText(getApplicationContext(), "New Password is necessary", Toast.LENGTH_SHORT).show();
            editTextNewPwd.setError("Please enter your new password");
            editTextNewPwd.requestFocus();
        } else if (TextUtils.isEmpty(userPwdConfNew)) {
            Toast.makeText(getApplicationContext(), "Please confirm your new password", Toast.LENGTH_SHORT).show();
            editTextConfirmPwd.setError("Please re-enter your new password");
            editTextConfirmPwd.requestFocus();
        } else if (!userNewPwd.equals(userPwdConfNew)) { // Changed from matches() to equals()
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            editTextConfirmPwd.setError("Passwords do not match");
            editTextConfirmPwd.requestFocus();
        } else if (userCurPwd.equals(userNewPwd)) { // Changed from matches() to equals()
            Toast.makeText(getApplicationContext(), "New password cannot be the same as the old password", Toast.LENGTH_SHORT).show();
            editTextConfirmPwd.setError("Please enter a new password");
            editTextConfirmPwd.requestFocus();
        } else {
            firebaseUser.updatePassword(userNewPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Password has been updated", Toast.LENGTH_SHORT).show();

                        // Navigate back to SettingsFragment through the hosting activity
                        Intent intent = new Intent(getApplicationContext(), Main.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
