package com.example.sanmarino;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;
import javafx.scene.image.ImageView;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private ImageView imgLogo;

    private Connection connect() {
        try {
            String url = "jdbc:ucanaccess://C:/Users/User/Documents/BARANGAYSYSTEM/Javafx-2.0-main/SANMARINO/BarangayDB/SanMarinoDB.accdb";
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println("CONNECTION FAILED! Reason: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void initialize() {
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String userIn = txtUsername.getText();
        String passIn = txtPassword.getText();
        User user = getUser(userIn);

        if (user == null) {
            lblError.setText("User not found.");
            return;
        }

        // Lockout Check
        if (user.getLockoutTime() != null && LocalDateTime.now().isBefore(user.getLockoutTime())) {
            lblError.setText("Locked. Try again later.");
            return;
        }

        if (passIn.equals(user.getPassword())) {
            updateStatus(userIn, 0, null);

            // --- THE CRITICAL FIX ---
            // Save the user data to the Session Manager BEFORE moving to the dashboard
            UserSession.init(user.getUsername(), user.getRoleId());

            proceedToDashboard(event, user);
        } else {
            int strikes = user.getFailedAttempts() + 1;
            LocalDateTime lockout = (strikes >= 5) ? LocalDateTime.now().plusMinutes(30) : null;
            updateStatus(userIn, strikes, lockout);
            lblError.setText(lockout != null ? "Locked for 30 mins." : "Wrong password. " + (5 - strikes) + " left.");
        }
    }

    /**
     * This replaces switchToMain and handleSuccessfulLogin.
     * It passes the actual database data to the smUiController.
     */
    private void proceedToDashboard(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HOME.fxml"));
            Parent root = loader.load();

            // Get the controller of the Home screen
            smUiController homeController = loader.getController();

            // Pass the REAL data from the database
            // username string and role_id integer
            homeController.setInitialData(user.getUsername(), user.getRoleId());

            // Switch the Scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User getUser(String username) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM USER WHERE Username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int roleFromDB = rs.getInt("Role_ID");
                System.out.println("DB Debug: Found Role_ID " + roleFromDB + " for user " + username);
                Timestamp ts = rs.getTimestamp("Lockout_Time");
                // Note: Ensure your User class constructor matches these 5 arguments
                return new User(
                        rs.getString("Username"),
                        rs.getString("Password"),
                        roleFromDB,
                        rs.getInt("Failed_Attempts"),
                        ts != null ? ts.toLocalDateTime() : null
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void updateStatus(String username, int attempts, LocalDateTime lockout) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("UPDATE USER SET Failed_Attempts=?, Lockout_Time=? WHERE Username=?")) {
            ps.setInt(1, attempts);
            ps.setTimestamp(2, lockout != null ? Timestamp.valueOf(lockout) : null);
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}