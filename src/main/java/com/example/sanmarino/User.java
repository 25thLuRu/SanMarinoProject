package com.example.sanmarino;

import java.time.LocalDateTime;


public class User {
    private String username;
    private String password;
    private int roleId;
    private int failedAttempts;
    private LocalDateTime lockoutTime;

    public User(String username, String password, int roleId, int failedAttempts, LocalDateTime lockoutTime) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.failedAttempts = failedAttempts;
        this.lockoutTime = lockoutTime;
    }
    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getRoleId() {return roleId;}
    public int getFailedAttempts() { return failedAttempts; }
    public LocalDateTime getLockoutTime() { return lockoutTime; }
}