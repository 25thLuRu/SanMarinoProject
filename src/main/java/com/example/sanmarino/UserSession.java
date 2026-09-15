package com.example.sanmarino;

public class UserSession {
    private static UserSession instance;

    private final String username;
    private final int roleId;

    private UserSession(String username, int roleId) {
        this.username = username;
        this.roleId = roleId;
    }

    // Call this once in LoginController after a successful login
    public static void init(String username, int roleId) {
        instance = new UserSession(username, roleId);
    }

    public static UserSession getInstance() {
        return instance;
    }

    // Call this during Logout
    public static void clear() {
        instance = null;
    }

    public String getUsername() { return username; }
    public int getRoleId() { return roleId; }
}