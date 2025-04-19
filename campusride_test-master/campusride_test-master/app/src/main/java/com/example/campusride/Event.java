package com.example.campusride;

public class Event {
    private String eventId;
    private String eventName;
    private String eventLocation;  // Ensure this is named correctly
    private String eventTime;

    public Event() {
        // Required for Firebase
    }

    public Event(String eventId, String eventName, String eventLocation, String eventTime) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventTime = eventTime;
    }

    // Getter for eventId
    public String getEventId() {
        return eventId;
    }

    // Getter for eventName
    public String getEventName() {
        return eventName;
    }

    // **🔥 Fix: Getter for eventLocation (was missing)**
    public String getEventLocation() {
        return eventLocation;
    }

    // **🔥 Fix: Getter for eventTime (was named incorrectly)**
    public String getEventTime() {
        return eventTime;
    }
}
