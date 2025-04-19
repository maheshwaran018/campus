package com.example.campusride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Signup extends AppCompatActivity {
    TextView t3;
    Button create;
    EditText mail, pass, ID, name;
    ProgressBar progressBar;
    Spinner roleSpinner, genderSpinner;
    String selectedRole = "", selectedGender = "";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Database Reference
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements
        t3 = findViewById(R.id.t3);
        create = findViewById(R.id.b1);
        mail = findViewById(R.id.mail);
        pass = findViewById(R.id.pass);
        ID = findViewById(R.id.ID);
        name = findViewById(R.id.name);
        progressBar = findViewById(R.id.pb2);
        roleSpinner = findViewById(R.id.roleSpinner);
        genderSpinner = findViewById(R.id.genderSpinner);

        // Setup Spinners
        setupRoleSpinner();
        setupGenderSpinner();

        // Redirect to login
        t3.setOnClickListener(view -> {
            startActivity(new Intent(Signup.this, login.class));
            finish();
        });

        // Create account on button click
        create.setOnClickListener(view -> validateAndCheckUser());
    }

    private void setupRoleSpinner() {
        List<String> roles = Arrays.asList("Select Role", "Driver", "Passenger");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(adapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = position == 0 ? "" : roles.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupGenderSpinner() {
        List<String> genders = Arrays.asList("Select Gender", "Male", "Female", "Other");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        genderSpinner.setAdapter(adapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = position == 0 ? "" : genders.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void validateAndCheckUser() {
        String email = mail.getText().toString().trim();
        String password = pass.getText().toString().trim();
        String registerNo = ID.getText().toString().toUpperCase().trim();
        String userName = name.getText().toString().trim();

        // Validate input
        if (!isInputValid(userName, registerNo, email, password, selectedRole, selectedGender)) {
            return;
        }

        // Show progress bar while creating account
        pbar(true);

        // Check if email or register number exists in Realtime Database
        databaseReference.child(registerNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    pbar(false);
                    ID.setError("Register No already exists");
                    ID.requestFocus();
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Signup.this, task -> {
                                pbar(false);
                                if (task.isSuccessful()) {
                                    saveUserData(userName, registerNo, email, selectedRole, selectedGender);
                                    sendVerificationEmail(task.getResult().getUser());
                                } else {
                                    Toast.makeText(Signup.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbar(false);
                Toast.makeText(Signup.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isInputValid(String userName, String registerNo, String email, String password, String role, String gender) {
        if (TextUtils.isEmpty(userName)) {
            name.setError("Name is required");
            name.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(registerNo) || registerNo.length() < 7 || registerNo.length() > 8) {
            ID.setError("Invalid Register No");
            ID.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mail.setError("Invalid Email");
            mail.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            pass.setError("Password is too short");
            pass.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(role)) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserData(String userName, String registerNo, String email, String role, String gender) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("Name", userName);
        userData.put("Register No", registerNo);
        userData.put("Email", email);
        userData.put("Role", role);
        userData.put("Gender", gender);
        userData.put("Rating", 0); // Default rating for drivers

        databaseReference.child(registerNo)
                .setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("register_no", registerNo);
                    editor.putString("role", role);
                    editor.apply();

                    Toast.makeText(Signup.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    startActivity(new Intent(Signup.this, login.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(Signup.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Signup.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Signup.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void pbar(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        create.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}
