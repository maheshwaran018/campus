package com.example.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);

        new Handler().postDelayed(this::checkUser, 1500);
    }

    private void checkUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(loading.this, login.class));
            finish();
        } else {
            fetchUserRole(user.getEmail().toLowerCase());  // Convert to lowercase to match Firebase
        }
    }

    private void fetchUserRole(String email) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.orderByChild("Email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                String role = data.child("Role").getValue(String.class);
                                if (role != null) {
                                    navigateToRoleActivity(role);
                                } else {
                                    handleUnknownRole();
                                }
                                return;
                            }
                        } else {
                            handleUnknownRole();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(loading.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToRoleActivity(String role) {
        Intent intent;
        if ("Driver".equalsIgnoreCase(role)) {
            intent = new Intent(loading.this, DriverActivity.class);
        } else if ("Passenger".equalsIgnoreCase(role)) {
            intent = new Intent(loading.this, PassengerActivity.class);
        } else {
            handleUnknownRole();
            return;
        }
        startActivity(intent);
        finish();
    }

    private void handleUnknownRole() {
        Toast.makeText(loading.this, "User role not found", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(loading.this, login.class));
        finish();
    }
}
