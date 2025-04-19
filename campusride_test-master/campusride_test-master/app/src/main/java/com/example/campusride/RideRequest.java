package com.example.campusride;
public class RideRequest {
    private String requestId;
    private String passengerId;
    private String eventName;
    private String eventId;
    private String pickupLocation;
    private String timing; // Add this line back
    private String status;

    // Default constructor (needed for Firebase)
    public RideRequest() {
    }

    // Constructor with all fields
    public RideRequest(String requestId, String passengerId, String eventName, String pickupLocation, String status, String timing, String eventId) {
        this.requestId = requestId;
        this.eventId = eventId;
        this.passengerId = passengerId;
        this.eventName = eventName;
        this.pickupLocation = pickupLocation;
        this.status = status;
        this.timing = timing;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getTiming() { // Add this getter
        return timing;
    }

    public void setTiming(String timing) { // Add this setter method
        this.timing = timing;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
