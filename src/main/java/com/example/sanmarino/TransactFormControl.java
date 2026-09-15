package com.example.sanmarino;

import java.sql.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.transformation.SortedList;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;

import java.util.Objects;
import java.util.ResourceBundle;



public class TransactFormControl implements Initializable{

    @FXML
    private TextField filterField;
    private Connection connect() {
        try {
            String url = "jdbc:ucanaccess://C:/Users/User/Documents/BARANGAYSYSTEM/Javafx-2.0-main/SANMARINO/BarangayDB/SanMarinoDB.accdb";
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public TableView<Person> tableview;


    public TableColumn<Person, Integer > colTransactID;
    public TableColumn<Person, Integer > colFormID;
    public TableColumn<Person, Double > colTransactAmount;
    public TableColumn<Person, String> colTransactMethod;
    @FXML private VBox navProfile;



    @FXML
    private ChoiceBox<String> ColChoiceBox;

    private String[] Colmodes = {"Transact ID", "Form ID","Amounts", "Payment Type"};



    @Override
    public void initialize(URL location, ResourceBundle resources) {


        colTransactID.setCellValueFactory(new PropertyValueFactory<>("PersonTransactID"));
        colFormID.setCellValueFactory(new PropertyValueFactory<>("PersonFormID"));
        colTransactAmount.setCellValueFactory(new PropertyValueFactory<>("PersonTransactAmount"));
        colTransactMethod.setCellValueFactory(new PropertyValueFactory<>("PersonTransactType"));
        tableview.setItems(observableList);

        FilteredList<Person> filteredData = new FilteredList<>(observableList, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(person -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                String selectedCategory = ColChoiceBox.getValue();

                if (selectedCategory == null) return true;

                // Only check the requested column
                switch (selectedCategory) {
                    case "Transact ID":
                        return String.valueOf(person.getPersonTransactID()).contains(lowerCaseFilter);
                    case "Form ID":
                        return String.valueOf(person.getPersonFormID()).contains(lowerCaseFilter);
                    case "Amounts":
                        return String.valueOf(person.getPersonTransactAmount()).contains(lowerCaseFilter);
                    case "Payment Type":
                        return String.valueOf(person.getPersonTransactType()).toLowerCase().contains(lowerCaseFilter);
                    default:
                        return false;
                }
            });
        });

        // 4. Wrap in SortedList so column clicking still works
        SortedList<Person> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());
        tableview.setItems(sortedData);

        // 5. Setup UI Components
        ColChoiceBox.getItems().addAll(Colmodes);
        ColChoiceBox.getSelectionModel().selectFirst();
        loadData();

        // 6. SESSION MANAGER: Automatic Admin Check
        UserSession session = UserSession.getInstance();
        if (session != null) {
            setInitialData(session.getRoleId());
        }
    }
    public void ColmodeType(ActionEvent event) {
        String paymentModeType = ColChoiceBox.getValue();
        System.out.println("payment method is " + paymentModeType);
    }

    ObservableList<Person> observableList= FXCollections.observableArrayList(

    );

    public void addPerson(Person person) {
        observableList.add(person);
    }

    public void loadData() {

        observableList.clear();

        try {
            Connection conn = connect();
            String sql = "SELECT * FROM TRANSACTION";
            assert conn != null;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                Person person = new Person(
                        rs.getInt("Transaction_ID"),
                        rs.getInt("Form_ID"),
                        rs.getDouble("Transaction_Amount"),
                        rs.getInt("PaymentType_ID")
                );

                observableList.add(person);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    void buttonRemove(ActionEvent event){
        Person selected = tableview.getSelectionModel().getSelectedItem();

        if (selected == null) return;
        Alert Remove = new Alert(Alert.AlertType.CONFIRMATION);
        Remove.setHeaderText("Are you sure this is the right data?");
        Remove.setContentText("If deleted, it will not be able to be restored");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (Remove.showAndWait().get() == ButtonType.OK) {
            try{
                Connection conn = connect();

                String sql = "DELETE FROM TRANSACTION WHERE Transaction_ID = ?";
                PreparedStatement sp = conn.prepareStatement(sql);
                sp.setInt(1, selected.getPersonTransactID());

                sp.executeUpdate();

                conn.close();

                loadData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }



    public void insertTransaction(int transId, int formId, double amount, int paymentType) {
        // We use [TRANSACTION] in brackets because TRANSACTION is a reserved SQL keyword
        String sql = "INSERT INTO [TRANSACTION] (Form_ID, Transaction_Amount, PaymentType_ID) VALUES ( ?, ?, ?)";

        try (Connection conn = connect()) {
            if (conn == null) {
                System.err.println("Connection is null. Check your driver and database path.");
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, formId);
                pstmt.setDouble(2, amount);
                pstmt.setInt(3, paymentType);

                int rows = pstmt.executeUpdate();

                System.out.println("DEBUG: Rows inserted: " + rows);

                loadData();

            }
        } catch (SQLException e) {
            System.err.println("SQL Error during insert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //MODAL FOR TFORMS
    @FXML
    private void AddTForm(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddTFormm.fxml"));
            Parent root = fxmlLoader.load();

            FormController controller = fxmlLoader.getController();
            controller.setMainController(this);  // IMPORTANT


            Stage stage = new Stage();
            stage.setTitle("My Form");
            stage.setScene(new Scene(root));


            stage.initModality(Modality.APPLICATION_MODAL);


            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void goAnnouncement(MouseEvent event) throws IOException {
        switchScene(event, "ANNOUNCEMENT.fxml");
    }

    @FXML
    public void goHome(MouseEvent event) throws IOException {
        switchScene(event, "HOME.fxml");

    }

    @FXML
    public void goRecords(MouseEvent event) throws IOException {

        System.out.println("Already on the Residents page.");
    }

    @FXML
    public void goAbout(MouseEvent event) throws IOException {
        switchScene(event, "ABOUT.fxml");

    }
     // The fx:id from Scene Builder

    public void setInitialData(int roleId) {
        if (navProfile != null) {
            // If Role ID is 1 (Admin), show it. Otherwise, hide and close the gap.
            navProfile.setVisible(roleId == 1);
            navProfile.setManaged(roleId == 1);
        }
    }

    // Helper method to keep your code clean
    private void switchScene(MouseEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void Logout(MouseEvent event) {
        Alert LogoutAlert = new Alert(Alert.AlertType.CONFIRMATION);
        LogoutAlert.setHeaderText("Leaving already?");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (LogoutAlert.showAndWait().get() == ButtonType.OK) {
            stage = (Stage) stage.getScene().getWindow();
            System.out.println("You've succesfully logged out");
            stage.close();
        }


    }

}














