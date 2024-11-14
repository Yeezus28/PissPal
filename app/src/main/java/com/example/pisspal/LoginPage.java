package com.example.pisspal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.PasswordAuthentication;

public class LoginPage extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    private EditText TextEmail;
    private EditText TextPassword;
    private TextView signup, Forgot_Pwd;
    private Button login;

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
        setContentView(R.layout.activity_login_page);
        mAuth = FirebaseAuth.getInstance();
        TextEmail = findViewById(R.id.input_email);
        TextPassword = findViewById(R.id.input_password);
        login = findViewById(R.id.button_login);
        login.setOnClickListener(onLogin);
        signup = findViewById(R.id.signup);
        signup.setOnClickListener(onSignup);
        Forgot_Pwd = findViewById(R.id.forgetPwd);
        Forgot_Pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgetPassword.class);
                startActivity(intent);
                finish();
            }
        });
        //signup.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private View.OnClickListener onLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email, password;
            email = String.valueOf(TextEmail.getText());
            password = String.valueOf(TextPassword.getText());
            if (TextUtils.isEmpty(email)||TextUtils.isEmpty(password)) {
                Toast.makeText(LoginPage.this, "Please fill in the necessary information.", Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Main.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginPage.this, "Login Unsuccessful.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    };
    private  View.OnClickListener onSignup = new  View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), RegisterPage.class);
            startActivity(intent);
        }
    };
}