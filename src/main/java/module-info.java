module scanhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.mkammerer.argon2.nolibs;
    requires com.microsoft.sqlserver.jdbc;
    requires java.sql;
    requires java.naming;

    exports ScanHub.GUI.controllers;
    opens ScanHub.GUI.controllers to javafx.fxml;
    exports ScanHub;
    opens ScanHub to javafx.fxml;
    exports ScanHub.DAL.interfaces;
    opens ScanHub.DAL.interfaces to javafx.fxml;
}