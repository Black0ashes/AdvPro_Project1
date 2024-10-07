module se233.imgdecrop {
    requires javafx.fxml;
    requires com.jfoenix;
    requires javafx.controls;
    requires javafx.swing;


    opens se233.imgdecrop.controller to javafx.fxml;
    exports se233.imgdecrop.controller;

    opens se233.imgdecrop to javafx.fxml;
    exports se233.imgdecrop;
}