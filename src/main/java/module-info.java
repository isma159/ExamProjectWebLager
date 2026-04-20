module dk.easv.examprojectweblager {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.mkammerer.argon2.nolibs;


    exports ScanHub.GUI.controllers;
    opens ScanHub.GUI.controllers to javafx.fxml;
    exports ScanHub;
    opens ScanHub to javafx.fxml;
}