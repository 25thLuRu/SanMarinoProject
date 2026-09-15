package com.example.sanmarino;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDateTime;

public class UserManagementController {

    @FXML private TableView<SystemUser> userTable;
    @FXML private TableColumn<SystemUser, Integer> colId;
    @FXML private TableColumn<SystemUser, String> colUsername;
    @FXML private TableColumn<SystemUser, String> colStatus;

    private final ObservableList<SystemUser> userList = FXCollections.observableArrayList();
    private final String dbUrl = "jdbc:ucanaccess://C:/Users/User/Documents/BARANGAYSYSTEM/Javafx-2.0-main/SANMARINO/BarangayDB/SanMarinoDB.accdb";

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT User_ID, Username, Last_Active FROM USER")) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("Last_Active");
                userList.add(new SystemUser(
                        rs.getInt("User_ID"),
                        rs.getString("Username"),
                        (ts != null) ? ts.toLocalDateTime() : null
                ));
            }
            userTable.setItems(userList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void handleAddUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Create a new account");
        dialog.setContentText("Username:");

        dialog.showAndWait().ifPresent(name -> {
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO USER (Username, [Password]) VALUES (?, '1234')")) {
                ps.setString(1, name);
                ps.executeUpdate();
                loadUsers();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    @FXML
    void handleDeleteUser() {
        SystemUser selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getUsername() + "?").showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM USER WHERE User_ID = ?")) {
                ps.setInt(1, selected.getUserId());
                ps.executeUpdate();
                loadUsers();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}