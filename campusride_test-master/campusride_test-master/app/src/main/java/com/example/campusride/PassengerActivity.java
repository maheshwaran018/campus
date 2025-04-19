package com.example.campusride;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PassengerActivity extends AppCompatActivity {

    private DatabaseReference rideRequestsRef, eventsRef;
    private FirebaseAuth auth;
    private String regNo;
    private ImageView menuButton;

    private CardView rideRequestBox;
    private TextView tvRequestEvent, tvRequestPickup, tvRequestTime;
    private FloatingActionButton btnCancelRide,fabAddEvent,btnBookRide;

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ArrayList<Event> eventList;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "RideRequestPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        auth = FirebaseAuth.getInstance();
        regNo = auth.getCurrentUser().getEmail().split("@")[0];
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests").child(regNo);
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        menuButton = findViewById(R.id.menuButton);
        rideRequestBox = findViewById(R.id.rideRequestBox);
        tvRequestEvent = findViewById(R.id.tvRequestEvent);
        tvRequestPickup = findViewById(R.id.tvRequestPickup);
        tvRequestTime = findViewById(R.id.tvRequestTime);
        btnCancelRide = findViewById(R.id.btnCancelRide);
        btnBookRide = findViewById(R.id.fabRequestRide);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        recyclerView = findViewById(R.id.eventRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this::bookRide); // Click listener for event
        recyclerView.setAdapter(eventAdapter);

        loadRideRequest();
        loadEvents();
       menuButton.setOnClickListener(v ->popup() );
        btnCancelRide.setOnClickListener(v -> cancelRideRequest());
        fabAddEvent.setOnClickListener(v -> showCreateEventDialog());
        btnBookRide.setOnClickListener(v -> showRideRequestPopup());
    }

    private void loadRideRequest() {
        rideRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String savedEvent = snapshot.child("eventName").getValue(String.class);
                    String savedPickup = snapshot.child("pickupLocation").getValue(String.class);
                    String savedTime = snapshot.child("time").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    if (savedEvent != null && savedPickup != null && savedTime != null) {
                        tvRequestEvent.setText("Event: " + savedEvent);
                        tvRequestPickup.setText("Pickup: " + savedPickup);
                        tvRequestTime.setText("Time: " + savedTime);
                        rideRequestBox.setVisibility(View.VISIBLE);

                        // Show cancel button only if the request is pending
                        if ("Pending".equals(status)) {
                            btnCancelRide.setVisibility(View.VISIBLE);
                        } else {
                            btnCancelRide.setVisibility(View.GONE);
                        }
                    } else {
                        rideRequestBox.setVisibility(View.GONE);
                    }
                } else {
                    rideRequestBox.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PassengerActivity.this, "Failed to load ride request", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Event event = data.getValue(Event.class);
                    eventList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PassengerActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRideRequest() {
        rideRequestsRef.removeValue().addOnSuccessListener(aVoid -> {
            sharedPreferences.edit().clear().apply();
            rideRequestBox.setVisibility(View.GONE);

            btnBookRide.setVisibility(View.VISIBLE);
            btnCancelRide.setVisibility(View.GONE);

            Toast.makeText(PassengerActivity.this, "Ride request canceled", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(PassengerActivity.this, "Failed to cancel ride", Toast.LENGTH_SHORT).show()
        );
    }

    private void popup() {
        PopupMenu popupMenu = new PopupMenu(this, menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.passenger_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_profile) {
                // Open Profile Activity
                Intent intent = new Intent(PassengerActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.menu_logout) {
                // Logout and return to login screen
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(PassengerActivity.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }



    private void bookRide(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Book Ride for " + event.getEventName());

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_request_ride, null);
        builder.setView(dialogView);

        EditText etPickupLocation = dialogView.findViewById(R.id.pickupLocation);
        EditText etTime = dialogView.findViewById(R.id.requestTiming);

        builder.setPositiveButton("Request Ride", (dialog, which) -> {
            String pickupLocation = etPickupLocation.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (pickupLocation.isEmpty() || time.isEmpty()) {
                Toast.makeText(PassengerActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> rideData = new HashMap<>();
            rideData.put("regNo", regNo);
            rideData.put("eventName", event.getEventName());
            rideData.put("pickupLocation", pickupLocation);
            rideData.put("time", time);
            rideData.put("status", "Pending");

            rideRequestsRef.setValue(rideData).addOnSuccessListener(aVoid -> {
                rideRequestBox.setVisibility(View.VISIBLE);
                tvRequestEvent.setText("Event: " + event.getEventName());
                tvRequestPickup.setText("Pickup: " + pickupLocation);
                tvRequestTime.setText("Time: " + time);

                sharedPreferences.edit()
                        .putString("eventName", event.getEventName())
                        .putString("pickupLocation", pickupLocation)
                        .putString("time", time)
                        .apply();

                // Hide Request Ride button, show Cancel button
                btnBookRide.setVisibility(View.GONE);
                btnCancelRide.setVisibility(View.VISIBLE);

                Toast.makeText(PassengerActivity.this, "Ride request sent", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e ->
                    Toast.makeText(PassengerActivity.this, "Failed to request ride", Toast.LENGTH_SHORT).show()
            );
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showRideRequestPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request a Ride");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_request_ride, null);
        builder.setView(dialogView);

        EditText etPickupLocation = dialogView.findViewById(R.id.pickupLocation);
        EditText etTime = dialogView.findViewById(R.id.requestTiming);

        builder.setPositiveButton("Request Ride", (dialog, which) -> {
            String pickupLocation = etPickupLocation.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (pickupLocation.isEmpty() || time.isEmpty()) {
                Toast.makeText(PassengerActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> rideData = new HashMap<>();
            rideData.put("Regno", regNo);
            rideData.put("pickupLocation", pickupLocation);
            rideData.put("time", time);
            rideData.put("status", "Pending");

            rideRequestsRef.setValue(rideData).addOnSuccessListener(aVoid -> {
                rideRequestBox.setVisibility(View.VISIBLE);
                tvRequestPickup.setText("Pickup: " + pickupLocation);
                tvRequestTime.setText("Time: " + time);

                sharedPreferences.edit()
                        .putString("pickupLocation", pickupLocation)
                        .putString("time", time)
                        .apply();

                // Hide Request Ride button, show Cancel button
                btnBookRide.setVisibility(View.GONE);
                btnCancelRide.setVisibility(View.VISIBLE);

                Toast.makeText(PassengerActivity.this, "Ride request sent", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e ->
                    Toast.makeText(PassengerActivity.this, "Failed to request ride", Toast.LENGTH_SHORT).show()
            );
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }



    private void showCreateEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Event");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_event, null);
        builder.setView(dialogView);

        EditText etEventName = dialogView.findViewById(R.id.eventName);
        EditText etEventLocation = dialogView.findViewById(R.id.eventLocation);
        EditText etEventTime = dialogView.findViewById(R.id.eventTiming);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String eventName = etEventName.getText().toString().trim();
            String eventLocation = etEventLocation.getText().toString().trim();
            String eventTime = etEventTime.getText().toString().trim();

            if (eventName.isEmpty() || eventLocation.isEmpty() || eventTime.isEmpty()) {
                Toast.makeText(PassengerActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventId = eventsRef.push().getKey();
            Event event = new Event(eventId, eventName, eventLocation, eventTime);

            eventsRef.child(eventId).setValue(event).addOnSuccessListener(aVoid ->
                    Toast.makeText(PassengerActivity.this, "Event created", Toast.LENGTH_SHORT).show()
            ).addOnFailureListener(e ->
                    Toast.makeText(PassengerActivity.this, "Failed to create event", Toast.LENGTH_SHORT).show()
            );
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
