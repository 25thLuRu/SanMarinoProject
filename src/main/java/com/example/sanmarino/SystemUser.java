package com.example.sanmarino;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SystemUser {
    private int userId;
    private String username;
    private LocalDateTime lastActive;

    public SystemUser(int userId, String username, LocalDateTime lastActive) {
        this.userId = userId;
        this.username = username;
        this.lastActive = lastActive;
    }

    // This is the logic the TableView will use for the "Status" column
    public String getStatus() {
        if (lastActive == null) return "Offline";

        // Calculate difference between now and their last click
        long minutes = ChronoUnit.MINUTES.between(lastActive, LocalDateTime.now());

        // If they were active within the last 3 minutes, show Online
        return (minutes < 3) ? "Online" : "Offline (" + minutes + "m ago)";
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
}