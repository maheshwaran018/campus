package com.example.campusride;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DriverDashboardActivity extends AppCompatActivity {

    private EditText passengerId1, gender1, passengerId2, gender2, passengerId3, gender3, passengerId4, gender4;
    private Button saveButton, clearButton;
    private DatabaseReference ridesRef;
    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        // Initialize Firebase
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get logged-in driver's ID
        ridesRef = FirebaseDatabase.getInstance().getReference("rides").child(driverId).child("seats");

        // Initialize Views
        passengerId1 = findViewById(R.id.passengerId1);
        gender1 = findViewById(R.id.gender1);
        passengerId2 = findViewById(R.id.passengerId2);
        gender2 = findViewById(R.id.gender2);
        passengerId3 = findViewById(R.id.passengerId3);
        gender3 = findViewById(R.id.gender3);
        passengerId4 = findViewById(R.id.passengerId4);
        gender4 = findViewById(R.id.gender4);
        saveButton = findViewById(R.id.saveButton);
        clearButton = findViewById(R.id.clearButton);

        // Save Button Click - Save Data to Firebase
        saveButton.setOnClickListener(v -> saveSeatsToFirebase());

        // Clear All Button Click - Clear Firebase & UI
        clearButton.setOnClickListener(v -> clearAllSeats());
    }

    private void saveSeatsToFirebase() {
        Map<String, Object> seatsData = new HashMap<>();
        seatsData.put("seat1/passengerId", passengerId1.getText().toString().trim());
        seatsData.put("seat1/gender", gender1.getText().toString().trim());
        seatsData.put("seat2/passengerId", passengerId2.getText().toString().trim());
        seatsData.put("seat2/gender", gender2.getText().toString().trim());
        seatsData.put("seat3/passengerId", passengerId3.getText().toString().trim());
        seatsData.put("seat3/gender", gender3.getText().toString().trim());
        seatsData.put("seat4/passengerId", passengerId4.getText().toString().trim());
        seatsData.put("seat4/gender", gender4.getText().toString().trim());

        ridesRef.updateChildren(seatsData)
                .addOnSuccessListener(aVoid -> Toast.makeText(DriverDashboardActivity.this, "Seats Saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(DriverDashboardActivity.this, "Failed to save seats", Toast.LENGTH_SHORT).show());
    }

    private void clearAllSeats() {
        ridesRef.removeValue().addOnSuccessListener(aVoid -> {
            // Clear UI fields
            passengerId1.setText("");
            gender1.setText("");
            passengerId2.setText("");
            gender2.setText("");
            passengerId3.setText("");
            gender3.setText("");
            passengerId4.setText("");
            gender4.setText("");

            Toast.makeText(DriverDashboardActivity.this, "All Seats Cleared", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(DriverDashboardActivity.this, "Failed to clear seats", Toast.LENGTH_SHORT).show());
    }
}
