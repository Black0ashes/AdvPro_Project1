package se233.imgdecrop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Launcher extends Application {
    public static int selectedAlgo;
    public static int counter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("edgeScene.fxml"));
                Scene scene = new Scene(root);
                Image icon = new Image(getClass().getResource("logo.png").toExternalForm());
                stage.getIcons().add(icon);
                stage.setScene(scene);
                stage.setTitle("DeCrop");
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
