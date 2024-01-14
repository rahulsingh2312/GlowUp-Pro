package com.example.glowup_pro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;
    private TextInputEditText nameEditText, ageEditText, genderEditText, weightEditText, emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressbar);
        nameEditText = findViewById(R.id.name);
        ageEditText = findViewById(R.id.age);
        genderEditText = findViewById(R.id.gender);
        weightEditText = findViewById(R.id.weight);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);

        Button registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String name = nameEditText.getText().toString().trim();
        final String age = ageEditText.getText().toString().trim();
        final String gender = genderEditText.getText().toString().trim();
        final String weight = weightEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || gender.isEmpty() || weight.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Registration.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                // Save user data to Firestore
                                saveUserDataToFirestore(user.getUid(), name, age, gender, weight, email);

                                Toast.makeText(Registration.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(Registration.this, "Registration failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String name, String age, String gender, String weight, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", age);
        userData.put("gender", gender);
        userData.put("weight", weight);
        userData.put("email", email);

        firestore.collection("users").document(userId)
                .set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                            // User data saved successfully
                        } else {
                            Toast.makeText(Registration.this, "Error saving user data to Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
