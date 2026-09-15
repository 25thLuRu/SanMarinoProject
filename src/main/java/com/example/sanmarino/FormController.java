    package com.example.sanmarino;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.ResourceBundle;

    public class FormController implements Initializable {
        @FXML
        private TextField getTransactionID;
        int TransID;

        @FXML
        private TextField getFormID;
        int FormID;


        //PayAmount

        @FXML
        private TextField transactAmountField;

        Double Amount;


        //datePicker
        @FXML
        private DatePicker getTransactionFormDPicker;


        public void getDate(ActionEvent event) {

        }

        //Paymentmodes


        @FXML
        private ChoiceBox<Integer> payChoiceBox;

        private Integer[] paymentModes = {1,2,3};

        @Override
        public void initialize(URL location, ResourceBundle resources) {

            payChoiceBox.getItems().addAll(paymentModes);
            payChoiceBox.setOnAction(this::PayModeTypes);

        }

        public void PayModeTypes(ActionEvent event) {
        }



        //Addbutton

        private TransactFormControl mainController;

        public void setMainController(TransactFormControl mainController) {
            this.mainController = mainController;
        }

        public void AddbuttonForAll(ActionEvent event) {
            try {

                Integer bayadMethod = payChoiceBox.getValue();
                Double amount = Double.parseDouble(transactAmountField.getText());

                int formID = Integer.parseInt(getFormID.getText());


                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Add to Database");
                alert.setContentText("Are you sure everything is correct?");

                if (alert.showAndWait().get() == ButtonType.OK) {
                    if (mainController != null) {
                        mainController.insertTransaction(0,formID, amount, bayadMethod);
                    }

                    // 4. Close the modal
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                }
            } catch (NumberFormatException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("Please ensure IDs and Amount are valid numbers.");
                error.show();
            }
        }

        }
