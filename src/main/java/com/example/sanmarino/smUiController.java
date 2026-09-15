  package com.example.sanmarino;

  import javafx.collections.FXCollections;
  import javafx.collections.ObservableList;
  import javafx.event.ActionEvent;
  import javafx.event.Event;
  import javafx.fxml.FXML;
  import javafx.fxml.FXMLLoader;
  import javafx.scene.Node;
  import javafx.scene.Parent;
  import javafx.scene.Scene;
  import javafx.scene.control.*;
  import javafx.scene.input.MouseEvent;
  import javafx.scene.layout.VBox;
  import javafx.stage.Stage;
  import java.io.IOException;
  import java.util.Objects;
  import java.sql.*;
  import java.time.LocalDateTime;
  import javafx.animation.KeyFrame;
  import javafx.animation.Timeline;
  import javafx.util.Duration;
  import javafx.scene.control.cell.PropertyValueFactory;

  public class smUiController {

    private Stage stage;
    private static final ObservableList<String> sharedMessages = FXCollections.observableArrayList();

    @FXML private TextField announcementInput;
    @FXML private VBox announcementContainer;
    @FXML private VBox homeAnnouncementContainer;
    @FXML private Label lblWelcome;
    @FXML private Label lblUserDisplay;
    @FXML private Node navProfile;
    @FXML private TableView<SystemUser> userTable; // Matches fx:id="userTable"
    @FXML private TableColumn<SystemUser, Integer> colId; // Matches fx:id="colId"
    @FXML private TableColumn<SystemUser, String> colUsername; // Matches fx:id="colUsername"
    @FXML private TableColumn<SystemUser, String> colStatus; // Matches fx:id="colStatus"

    private final ObservableList<SystemUser> userList = FXCollections.observableArrayList();
    private final String dbUrl = "jdbc:ucanaccess://C:/Users/User/Documents/BARANGAYSYSTEM/Javafx-2.0-main/SANMARINO/BarangayDB/SanMarinoDB.accdb";


    public void initialize() {
      // 1. Refresh announcements

      loadAnnouncementsFromDB();


      // 2. Automatically load session data
      UserSession session = UserSession.getInstance();
      if (session != null) {
        applySessionData(session.getUsername(), session.getRoleId());
        updateUserHeartbeat(session.getUsername());
      }

      // --- USER MANAGEMENT INITIALIZATION ---
      if (userTable != null) {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadUsers();

        // Auto-refresh the table every 10 seconds to catch "Online" status changes
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(10), e -> loadUsers()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
      }
    }
    private void loadUsers() {
      if (userTable == null) return;
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
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    @FXML
    void handleDeleteUser(ActionEvent event) {
      SystemUser selected = userTable.getSelectionModel().getSelectedItem();
      if (selected == null) return;

      // Safety: Prevent deleting your own account
      if (selected.getUsername().equals(UserSession.getInstance().getUsername())) {
        new Alert(Alert.AlertType.WARNING, "You cannot delete your own account.").show();
        return;
      }

      Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getUsername() + "?");
      if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM USER WHERE User_ID = ?")) {
          ps.setInt(1, selected.getUserId());
          ps.executeUpdate();
          loadUsers();
        } catch (SQLException e) { e.printStackTrace(); }
      }
    }

    private void updateUserHeartbeat(String username) {
      try (Connection conn = DriverManager.getConnection(dbUrl);
           PreparedStatement ps = conn.prepareStatement("UPDATE USER SET Last_Active = ? WHERE Username = ?")) {
        ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(2, username);
        ps.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Heartbeat failed.");
      }
    }
    private void applySessionData(String username, int roleId) {
      // Set Username Display
      if (lblUserDisplay != null && username != null) {
        String formatted = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        lblUserDisplay.setText(formatted);
      }

      // Admin Visibility Logic
      if (navProfile != null) {
        boolean isAdmin = (roleId == 1);
        navProfile.setVisible(isAdmin);
        navProfile.setManaged(isAdmin);
      }
    }

    @FXML private TableView<?> table;
    @FXML private TextField searchField;

    @FXML
    void openAddPopup(ActionEvent event) {
      System.out.println("Add Resident Clicked");
    }

    @FXML
    void openEditPopup(ActionEvent event) {
      System.out.println("Edit Resident Clicked");
    }

    @FXML
    void deleteResident(ActionEvent event) {
      System.out.println("Delete Resident Clicked");
    }


    private void navigateTo(Event event, String fxmlFile) {
      try {
        // 1. Get the current Stage from the event
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 2. Heartbeat update
        UserSession session = UserSession.getInstance();
        if (session != null) {
          updateUserHeartbeat(session.getUsername());
        }

        // 3. Load the FXML
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));

        // 4. Handle Maximization for Records
        if (fxmlFile.equalsIgnoreCase("RECORDSCOPY.fxml")) {
          stage.setMaximized(true);
        } else if(fxmlFile.equalsIgnoreCase("Login.fxml")){
          stage.setWidth(400); // Adjust to your preferred Login width
          stage.setHeight(600); // Adjust to your preferred Login height
          stage.centerOnScreen();
        } else {
          stage.setMaximized(false);
        }

        // 5. THE FIX: Get the existing scene and just swap the root
        Scene currentScene = stage.getScene();
        if (currentScene == null) {
          // If it's the very first window, create the scene
          stage.setScene(new Scene(root));
        } else {
          // If the scene exists, just change the layout inside it
          currentScene.setRoot(root);
        }

        stage.show();

      } catch (IOException e) {
        System.err.println("Error loading " + fxmlFile);
        e.printStackTrace();
      }
    }

    @FXML void switchToAnnouncement(MouseEvent event) { navigateTo(event, "ANNOUNCEMENT.fxml"); }
    @FXML void switchToHOME(Event e) { navigateTo(e, "HOME.fxml"); }
    @FXML void switchToAbout(Event e) { navigateTo(e, "ABOUT.fxml"); }
    @FXML void switchToRecords(Event e) { navigateTo(e, "RECORDSCOPY.fxml"); }
    @FXML void switchToPROFILE(MouseEvent e) { navigateTo(e, "PROFILE.fxml"); }



    @FXML public void goAnnouncement(MouseEvent event) { navigateTo(event, "ANNOUNCEMENT.fxml"); }
    @FXML public void goHome(MouseEvent event) { navigateTo(event, "HOME.fxml"); }
    @FXML public void goRecords(MouseEvent event) { navigateTo(event, "RECORDSCOPY.fxml"); }
    @FXML public void goAbout(MouseEvent event) { navigateTo(event, "ABOUT.fxml"); }

    public void setInitialData(String username, int roleId) {
      // 1. Set the Welcome Name

      if (username != null && !username.isEmpty()) {
        // Capitalize first letter (e.g., "admin" -> "Admin")

        if (lblWelcome != null) { // This prevents the crash!
          lblWelcome.setText("WELCOME");
          if (lblUserDisplay != null) {
            String formatted = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
            lblUserDisplay.setText(formatted);
          }

        } else {
          System.err.println("Error: lblWelcome is null! Check fx:id in Scene Builder.");
        }
      }

      // 2. Admin Visibility Logic (Assuming 1 = Admin)
      if (navProfile != null) {
        // Debugging: This will print the ID to your console so you can see what the DB is sending
        System.out.println("Login Debug: Received Role ID = " + roleId);

        if (roleId == 1) {
          // Only show for ID 1
          navProfile.setVisible(true);
          navProfile.setManaged(true);
        } else {
          // Hide for everyone else
          navProfile.setVisible(false);
          navProfile.setManaged(false);
        }

      }

    }
    @FXML
    void handleAddUser(ActionEvent event) {
      // 1. Create the custom dialog
      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle("Add New User");
      dialog.setHeaderText("Enter Account Details");

      // 2. Set the button types (OK and Cancel)
      ButtonType loginButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

      // 3. Create the input fields
      TextField usernameField = new TextField();
      usernameField.setPromptText("Username");

      PasswordField passwordField = new PasswordField();
      passwordField.setPromptText("Password");

      ComboBox<String> roleComboBox = new ComboBox<>();
      roleComboBox.getItems().addAll("Admin", "Personnel");
      roleComboBox.setValue("Personnel"); // Default value

      // 4. Layout the fields in a VBox
      VBox content = new VBox(10);
      content.getChildren().addAll(
              new Label("Username:"), usernameField,
              new Label("Password:"), passwordField,
              new Label("Role:"), roleComboBox
      );
      dialog.getDialogPane().setContent(content);

      // 5. Handle the result
      dialog.showAndWait().ifPresent(response -> {
        if (response == loginButtonType) {
          String user = usernameField.getText();
          String pass = passwordField.getText();
          // Convert selection to ID (Admin = 1, Personnel = 2)
          int roleId = roleComboBox.getValue().equals("Admin") ? 1 : 2;

          if (!user.isEmpty() && !pass.isEmpty()) {
            saveUserToDatabase(user, pass, roleId);
          } else {
            new Alert(Alert.AlertType.ERROR, "All fields are required!").show();
          }
        }
      });
    }

    // Separate method to keep code clean
    private void saveUserToDatabase(String user, String pass, int roleId) {
      String sql = "INSERT INTO [USER] (Username, [Password], Role_ID) VALUES (?, ?, ?)";

      try (Connection conn = DriverManager.getConnection(dbUrl);
           PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, user);
        ps.setString(2, pass);
        ps.setInt(3, roleId);
        ps.executeUpdate();
        loadUsers(); // Refresh Table
      } catch (SQLException e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
      }
    }


    @FXML
    void handlePostAnnouncement(Event event) {
      String message = announcementInput.getText();
      if (message != null && !message.trim().isEmpty()) {
        saveAnnouncementToDB(message.trim());
        announcementInput.clear();
        loadAnnouncementsFromDB(); // Reload and refresh the UI
      }
    }
    private void saveAnnouncementToDB(String message) {
      String sql = "INSERT INTO ANNOUNCEMENT (Announcement_Message, Date_Posted) VALUES (?, ?)";
      try (Connection conn = DriverManager.getConnection(dbUrl);
           PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, message);
        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        ps.executeUpdate();
      } catch (SQLException e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, "Failed to save announcement.").show();
      }
    }


    private void loadAnnouncementsFromDB() {
      // Clear existing UI elements
      if (announcementContainer != null) announcementContainer.getChildren().clear();
      if (homeAnnouncementContainer != null) homeAnnouncementContainer.getChildren().clear();

      String sql = "SELECT * FROM ANNOUNCEMENT ORDER BY Date_Posted DESC";
      try (Connection conn = DriverManager.getConnection(dbUrl);
           Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
          int id = rs.getInt("Announcement_ID");
          String msg = rs.getString("Announcement_Message");
          if(msg == null || msg.isEmpty()) {
            msg = "[No Content]"; // Temporary debug text to see if it's pulling nulls
          }

          // Add to the UI containers
          VBox card = createAnnouncementCard(id, msg);
          if (announcementContainer != null) {
            announcementContainer.getChildren().add(card);
          }
          // Create a second instance for the Home container (nodes can't be in two places)
          if (homeAnnouncementContainer != null) {
            homeAnnouncementContainer.getChildren().add(createAnnouncementCard(id, msg));
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    private VBox createAnnouncementCard(int id, String message) {
      VBox cardDiv = new VBox();
      cardDiv.setSpacing(10);
      // Add a border so you can see the card boundaries
      cardDiv.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #000; -fx-border-width: 1;");

      Label content = new Label("📌 " + message);
      content.setWrapText(true); // Allow text to go to next line
      content.setMaxWidth(Double.MAX_VALUE); // Let it grow
      content.setStyle("-fx-text-fill: black; -fx-font-size: 14px;"); // Ensure text is black

      // This line is key: ensures the VBox grows to fit the label
      VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);

      Button btnRemove = new Button("Remove");
      btnRemove.setOnAction(e -> {
        deleteAnnouncementFromDB(id);
        loadAnnouncementsFromDB();
      });

      cardDiv.getChildren().addAll(content, btnRemove);
      return cardDiv;
    }


    private void deleteAnnouncementFromDB(int id) {
      String sql = "DELETE FROM ANNOUNCEMENT WHERE Announcement_ID = ?";
      try (Connection conn = DriverManager.getConnection(dbUrl);
           PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        ps.executeUpdate();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    // --- HOVER EFFECTS ---
    @FXML
    void handleMouseEnter(MouseEvent event) {
      ((Node) event.getSource()).setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;");
    }

    @FXML
    void handleMouseExit(MouseEvent event) {
      ((Node) event.getSource()).setStyle("-fx-background-color: #148C9E; -fx-font-size: 20; -fx-font-weight: bold; -fx-padding: 0 0 0 20;");
    }

    @FXML
    void handleLogout(Event event) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?", ButtonType.OK, ButtonType.CANCEL);
      if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
        UserSession.clear(); // Wipe the session
        navigateTo(event, "Login.fxml");
      }
    }
  }



