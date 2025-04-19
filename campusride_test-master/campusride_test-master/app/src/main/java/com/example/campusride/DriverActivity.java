package com.example.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DriverActivity extends AppCompatActivity {

    private ImageView menuIcon;
    private TextView ratingText;
    private EditText availableSeatsEditText;
    private Button updateSeatsButton;
    private RecyclerView rideRequestsRecycler;

    private FirebaseAuth auth;
    private DatabaseReference userRef, rideRequestsRef, acceptedRidesRef, ridesRef;
    private RideRequestAdapter adapter;
    private ArrayList<RideRequest> rideRequestsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        // Initialize UI elements
        menuIcon = findViewById(R.id.menuIcon);
        ratingText = findViewById(R.id.ratingText);
        availableSeatsEditText = findViewById(R.id.availableSeatsEditText);
        updateSeatsButton = findViewById(R.id.updateSeatsButton);
        rideRequestsRecycler = findViewById(R.id.rideRequestsRecycler);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        String driverId = auth.getCurrentUser().getUid(); // Get logged-in driver's ID
        userRef = FirebaseDatabase.getInstance().getReference("users").child(driverId);
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        acceptedRidesRef = FirebaseDatabase.getInstance().getReference("acceptedRides");
        ridesRef = FirebaseDatabase.getInstance().getReference("rides");

        // Setup RecyclerView
        rideRequestsRecycler.setLayoutManager(new LinearLayoutManager(this));
        rideRequestsList = new ArrayList<>();
        adapter = new RideRequestAdapter(rideRequestsList, rideRequestsRef, acceptedRidesRef, this::acceptRideRequest);
        rideRequestsRecycler.setAdapter(adapter);

        // Load driver details and ride requests
        loadDriverData();
        loadRideRequests();

        // Menu click listener
        menuIcon.setOnClickListener(view -> showMenu());

        // Update available seats button
        updateSeatsButton.setOnClickListener(v -> updateAvailableSeats());
    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(this, menuIcon);
        popupMenu.getMenuInflater().inflate(R.menu.driver_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.dashboard) {
                startActivity(new Intent(DriverActivity.this, DriverDashboardActivity.class));
            } else if (id == R.id.profile) {
                startActivity(new Intent(DriverActivity.this, ProfileActivity.class));
            } else if (id == R.id.logout) {
                auth.signOut();
                startActivity(new Intent(DriverActivity.this, login.class));
                finish();
            }
            return true;
        });

        popupMenu.show();
    }

    private void loadDriverData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String rating = snapshot.child("Rating").getValue(String.class);
                    ratingText.setText("Rating: " + (rating != null ? rating : "N/A"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRideRequests() {
        rideRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rideRequestsList.clear();
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    RideRequest request = requestSnapshot.getValue(RideRequest.class);
                    if (request != null) {
                        rideRequestsList.add(request);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DriverActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAvailableSeats() {
        String seatsStr = availableSeatsEditText.getText().toString().trim();
        if (!seatsStr.isEmpty()) {
            int availableSeats = Integer.parseInt(seatsStr);
            ridesRef.child(auth.getCurrentUser().getUid()).child("availableSeats").setValue(availableSeats)
                    .addOnSuccessListener(aVoid -> Toast.makeText(DriverActivity.this, "Seats updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(DriverActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Enter number of seats", Toast.LENGTH_SHORT).show();
        }
    }

    private void acceptRideRequest(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Invalid Ride Request ID", Toast.LENGTH_SHORT).show();
            return;
        }

        rideRequestsRef.child(requestId).child("status").setValue("Accepted")
                .addOnSuccessListener(aVoid -> Toast.makeText(DriverActivity.this, "Ride request accepted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(DriverActivity.this, "Failed to accept ride", Toast.LENGTH_SHORT).show());
    }

}
