module dk.easv.examprojectweblager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires de.mkammerer.argon2.nolibs;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;


    exports ScanHub.GUI.controllers;
    opens ScanHub.GUI.controllers to javafx.fxml;
    exports ScanHub;
    opens ScanHub to javafx.fxml;
}