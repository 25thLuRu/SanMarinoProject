    package com.example.sanmarino;

    import javafx.application.Application;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.TableColumn;
    import javafx.scene.control.TableView;
    import javafx.stage.Stage;

    import java.io.IOException;
    import java.util.Objects;

    public class    smApplication extends Application {
        @Override
        public void start(Stage sisonStage) throws IOException {

            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("HOME.fxml")));
                Scene sisonScene = new Scene(root);
                sisonStage.setScene(sisonScene);
                String css =  this.getClass().getResource("UI.css").toExternalForm();
                sisonScene.getStylesheets().add(css);
                sisonStage.show();
                sisonStage.setMaximized(true);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }
