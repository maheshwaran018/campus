package com.example.campusride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView textName, textRegisterNo, textRole, textGender;
    private Button btnEdit;
    private DatabaseReference userRef;
    private String registerNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Views
        textName = findViewById(R.id.textName);
        textRegisterNo = findViewById(R.id.textRegisterNo);
        textRole = findViewById(R.id.textRole);
        textGender = findViewById(R.id.textGender);
        btnEdit = findViewById(R.id.btnEdit);

        // Retrieve Register No from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        registerNo = sharedPreferences.getString("roll no", null);

        if (registerNo != null) {
            // Reference the user in Firebase by Register No
            userRef = FirebaseDatabase.getInstance().getReference("users").child(registerNo);
            loadUserProfile();
        } else {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
        }

        // Open EditProfileActivity when "Edit" is clicked
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    textName.setText(snapshot.child("Name").getValue(String.class));
                    textRegisterNo.setText(snapshot.child("Register No").getValue(String.class));
                    textRole.setText(snapshot.child("Role").getValue(String.class));
                    textGender.setText(snapshot.child("Gender").getValue(String.class));
                } else {
                    Toast.makeText(ProfileActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
