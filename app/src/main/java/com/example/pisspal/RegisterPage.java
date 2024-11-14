    package com.example.pisspal;

    import android.content.Intent;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    public class RegisterPage extends AppCompatActivity {

        EditText TextEmail, TextPass, TextRePass;
        ImageButton backButton;
        Button butReg;
        FirebaseAuth mAuth;

        @Override
        public void onStart() {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null){
                Intent intent = new Intent(getApplicationContext(), Main.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register_page);
            mAuth = FirebaseAuth.getInstance();
            TextEmail = findViewById(R.id.input_email);
            TextPass = findViewById(R.id.input_password);
            TextRePass = findViewById(R.id.input_Repassword);
            butReg = findViewById(R.id.button_Register);
            backButton = findViewById(R.id.backButton);
            backButton.setOnClickListener(goBack);
            butReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email, password, repassword;
                    email = String.valueOf(TextEmail.getText());
                    password = String.valueOf(TextPass.getText());
                    repassword = String.valueOf(TextRePass.getText());

    /*                if (TextUtils.isEmpty(email)) {
                        Toast.makeText(RegisterPage.this, "Enter Email", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(RegisterPage.this, "Enter Password", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (TextUtils.isEmpty(repassword)) {
                        Toast.makeText(RegisterPage.this, "ReType Password", Toast.LENGTH_LONG).show();
                        return;
                    }*/
                    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword)) {
                        Toast.makeText(getApplicationContext(), "Please fill in the necessary information.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!password.equals(repassword)) {
                        Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Account Created Successfully!",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), LoginPage.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(RegisterPage.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }
            private View.OnClickListener goBack = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegisterPage.this, LoginPage.class);
                    startActivity(intent);
                    finish();
                }
            };
    }