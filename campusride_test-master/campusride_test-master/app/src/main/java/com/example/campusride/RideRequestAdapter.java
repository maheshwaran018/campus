package com.example.campusride;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusride.R;
import com.example.campusride.RideRequest;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class RideRequestAdapter extends RecyclerView.Adapter<RideRequestAdapter.ViewHolder> {
    private List<RideRequest> rideRequests;
    private DatabaseReference rideRequestsRef, acceptedRidesRef;
    private OnAcceptRideRequestListener acceptListener;

    public interface OnAcceptRideRequestListener {
        void onAcceptRide(String requestId);
    }

    public RideRequestAdapter(List<RideRequest> rideRequests, DatabaseReference rideRequestsRef, DatabaseReference acceptedRidesRef, OnAcceptRideRequestListener acceptListener) {
        this.rideRequests = rideRequests;
        this.rideRequestsRef = rideRequestsRef;
        this.acceptedRidesRef = acceptedRidesRef;
        this.acceptListener = acceptListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideRequest request = rideRequests.get(position);
        holder.pickupLocation.setText("Pickup: " + request.getPickupLocation());
        holder.timing.setText("Timing: " + request.getTiming());

        holder.acceptButton.setOnClickListener(v -> {
            if (acceptListener != null) {
                acceptListener.onAcceptRide(request.getRequestId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return rideRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pickupLocation, timing;
        Button acceptButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupLocation = itemView.findViewById(R.id.pickupLocation);
            timing = itemView.findViewById(R.id.tvTime);
            acceptButton = itemView.findViewById(R.id.acceptButton);
        }
    }
}
