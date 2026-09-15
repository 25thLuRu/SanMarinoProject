package com.example.sanmarino;

import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.input.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

public class HelloController {

    @FXML private TableView<Resident> table;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;

    @FXML private TableColumn<Resident, String> idCol, lastCol, middleCol,
            firstCol, contactCol, emailCol,
            houseCol, streetCol, civilCol, birthCol;

    private final ObservableList<Resident> list = FXCollections.observableArrayList();
    private FilteredList<Resident> filtered;

    // Database URL - Ensure this path is correct for your machine
    private final String dbUrl = "jdbc:ucanaccess://C:/Users/User/Documents/BARANGAYSYSTEM/Javafx-2.0-main/SANMARINO/BarangayDB/SanMarinoDB.accdb";

    @FXML
    public void initialize() {
        // Setup Columns
        idCol.setCellValueFactory(d -> d.getValue().idProperty());
        lastCol.setCellValueFactory(d -> d.getValue().lastProperty());
        middleCol.setCellValueFactory(d -> d.getValue().middleProperty());
        firstCol.setCellValueFactory(d -> d.getValue().firstProperty());
        contactCol.setCellValueFactory(d -> d.getValue().contactProperty());
        emailCol.setCellValueFactory(d -> d.getValue().emailProperty());
        houseCol.setCellValueFactory(d -> d.getValue().houseProperty());
        streetCol.setCellValueFactory(d -> d.getValue().streetProperty());
        civilCol.setCellValueFactory(d -> d.getValue().civilProperty());
        birthCol.setCellValueFactory(d -> d.getValue().birthProperty());

        filterBox.getItems().addAll("ID", "Last Name","Middle Name", "First Name", "Contact", "Email", "House Num","Street", "Civil", "Birth");
        filterBox.setValue("Last Name");

        filtered = new FilteredList<>(list, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterBox.valueProperty().addListener((obs, o, n) -> {
            applyFilter();
            applySort();
        });

        table.setItems(filtered);

        // LOAD DATA FROM DATABASE ON STARTUP
        loadResidentsFromDB();

        applySort();
    }

    private void loadResidentsFromDB() {
        list.clear();
        // Change [RESIDENTS] to your actual table name in Access
        String sql = "SELECT * FROM RESIDENT";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {

                int statusInt = rs.getInt("CivilStatus_ID");
                String statusStr = switch (statusInt) {
                    case 1 -> "Single";
                    case 2 -> "Married";
                    case 3 -> "Widowed";
                    default -> "Single";
                };


                list.add(new Resident(
                        rs.getString("Resident_ID"),
                        rs.getString("RL_Name"),
                        rs.getString("RM_Name"),
                        rs.getString("RF_Name"),
                        rs.getString("RContact_Num"),
                        rs.getString("REmail"),
                        rs.getString("House_Num"),
                        rs.getString("Street"),
                        statusStr,
                        rs.getString("Date_of_Birth")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error: Could not load data.");
        }
    }
    private void applyFilter() {
        // 1. Get the current search text and the category selected in the ComboBox
        String keyword = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();
        String filterType = filterBox.getValue();

        // 2. Update the predicate of the FilteredList
        filtered.setPredicate(resident -> {
            // If search box is empty, show everyone
            if (keyword.isEmpty()) {
                return true;
            }

            // 3. Compare the keyword against the specific field selected
            return switch (filterType) {
                case "ID"         -> resident.getId().toLowerCase().contains(keyword);
                case "Last Name"  -> resident.getLast().toLowerCase().contains(keyword);
                case "Middle Name" -> resident.getMiddle().toLowerCase().contains(keyword);
                case "First Name" -> resident.getFirst().toLowerCase().contains(keyword);
                case "Contact"    -> resident.getContact().toLowerCase().contains(keyword);
                case "Email"      -> resident.getEmail().toLowerCase().contains(keyword);
                case "House Num"  -> resident.getHouse().toLowerCase().contains(keyword);
                case "Street"     -> resident.getStreet().toLowerCase().contains(keyword);
                case "Civil"      -> resident.getCivil().toLowerCase().contains(keyword);
                case "Birth"   -> resident.getBirth().toLowerCase().contains(keyword);

                default           -> true;
            };
        });
    }

    private void applySort() {
        String filter = filterBox.getValue();
        if (filter == null) return;

        Comparator<Resident> comparator = switch (filter) {
            case "ID" -> Comparator.comparing(r -> {
                try {
                    return Integer.parseInt(r.getId());
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE;
                }
            });

            case "Last Name"   -> Comparator.comparing(r -> r.getLast().toLowerCase());
            case "Middle Name" -> Comparator.comparing(r -> r.getMiddle().toLowerCase());
            case "First Name"  -> Comparator.comparing(r -> r.getFirst().toLowerCase());
            case "Contact"     -> Comparator.comparing(r -> r.getContact().toLowerCase());
            case "Email"       -> Comparator.comparing(r -> r.getEmail().toLowerCase());
            case "House Num"   -> Comparator.comparing(r -> r.getHouse().toLowerCase());
            case "Street"      -> Comparator.comparing(r -> r.getStreet().toLowerCase());
            case "Civil"       -> Comparator.comparing(r -> r.getCivil().toLowerCase());
            case "Birth"    -> Comparator.comparing(r -> r.getBirth().toLowerCase());
            default -> null;
        };

        if (comparator != null) {
            // We sort the source 'list' so the 'filtered' list reflects the new order
            FXCollections.sort(list, comparator);
        }
    }

    private void showForm(Resident existing) {
        Stage popup = new Stage();
        popup.setTitle(existing == null ? "Add Resident" : "Edit Resident");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField idField = new TextField();
        idField.setEditable(false);
        idField.setDisable(true);

        TextField last = new TextField();
        TextField middle = new TextField();
        TextField first = new TextField();

        TextField contact = new TextField();
        TextField email = new TextField();
        TextField houseNumber = new TextField();
        TextField street = new TextField();
        ComboBox<String> civil = new ComboBox<>();
        civil.getItems().addAll("Single", "Married", "Widowed", "Divorced", "Legally Separated");
        DatePicker birth = new DatePicker();

        if (existing != null) {
            idField.setText(existing.getId()); // ID usually shouldn't change during edit
            last.setText(existing.getLast());
            middle.setText(existing.getMiddle());
            first.setText(existing.getFirst());

            contact.setText(existing.getContact());
            email.setText(existing.getEmail());
            houseNumber.setText(existing.getHouse());
            street.setText(existing.getStreet());
            civil.setValue(existing.getCivil());
            if (existing.getBirth() != null && !existing.getBirth().isEmpty()) {
                try { birth.setValue(LocalDate.parse(existing.getBirth())); } catch (Exception e) {}
            }
        }

        if (existing != null) {
            grid.addRow(0, new Label("Resident ID (Auto)"), idField);
        }
        grid.addRow(1, new Label("Last Name"), last);
        grid.addRow(2, new Label("Middle Name"), middle);
        grid.addRow(3, new Label("First Name"), first);

        grid.addRow(5, new Label("Contact"), contact);
        grid.addRow(6, new Label("Email"), email);
        grid.addRow(7, new Label("Civil Status"), civil);
        grid.addRow(8, new Label("House Number"), houseNumber);
        grid.addRow(9, new Label("Street"), street);
        grid.addRow(10, new Label("Date of Birth"), birth);

        Button save = new Button("Save");
        save.setOnAction(e -> {
            if (first.getText().isEmpty()) {
                showAlert("First Name is required.");
                return;
            }

            String bDay = (birth.getValue() == null) ? "" : birth.getValue().toString();

            int civilInt = switch (civil.getValue()) {
                case "Single" -> 1;
                case "Married" -> 2;
                case "Widowed" -> 3;
                case "Divorced" -> 4;
                case "Legally Separated" -> 5;
                default -> 1;
            };

            if (existing == null) {
                // DATABASE INSERT: Removed ID and the first '?'
                String sql = "INSERT INTO RESIDENT (RL_Name, RM_Name, RF_Name, RContact_Num, REmail, House_Num, Street, CivilStatus_ID, Date_of_Birth) VALUES (?,?,?,?,?,?,?,?,?)";

                executeSql(sql, last.getText(), middle.getText(), first.getText(),
                        contact.getText(), email.getText(), houseNumber.getText(),
                        street.getText(), civilInt, bDay);
            } else {
                // DATABASE UPDATE: Keep ID at the end to identify the record
                String sql = "UPDATE RESIDENT SET RL_Name=?, RM_Name=?, RF_Name=?,  RContact_Num=?, REmail=?, House_Num=?, Street=?, CivilStatus_ID=?, Date_of_Birth=? WHERE Resident_ID=?";

                executeSql(sql, last.getText(), middle.getText(), first.getText(),
                        contact.getText(), email.getText(), houseNumber.getText(),
                        street.getText(), civilInt, bDay, existing.getId());
            }

            loadResidentsFromDB();
            popup.close();
        });

        VBox layout = new VBox(15, grid, save);
        layout.setAlignment(Pos.CENTER);
        popup.setScene(new Scene(layout, 360, 520));
        popup.show();
    }


    private void executeSql(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                // setObject works for both Strings and Integers!
                ps.setObject(i + 1, params[i]);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("SQL Error: " + e.getMessage());
        }
    }

    @FXML
    public void deleteResident() {
        Resident selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a resident first."); return; }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this resident?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM RESIDENT WHERE Resident_ID= ?";
                executeSql(sql, selected.getId());
                loadResidentsFromDB();
            }
        });
    }

    // --- NAVIGATION ---
    private void switchScene(Event event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML public void goAnnouncement(MouseEvent e) throws IOException { switchScene(e, "ANNOUNCEMENT.fxml"); }
    @FXML public void goHome(MouseEvent e) throws IOException { switchScene(e, "HOME.fxml"); }
    @FXML public void goRecords(MouseEvent e) throws IOException { switchScene(e, "RECORDSCOPY.fxml"); }
    @FXML public void goAbout(MouseEvent e) { System.out.println("Already on About/Records"); }


    private void showAlert(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    @FXML public void openAddPopup() { showForm(null); }
    @FXML public void openEditPopup() { Resident s = table.getSelectionModel().getSelectedItem(); if(s!=null) showForm(s); else showAlert("Select a resident."); }
}