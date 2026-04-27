module scanhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.mkammerer.argon2.nolibs;
    requires com.microsoft.sqlserver.jdbc;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.sql;
    requires java.naming;
    requires java.desktop;
    requires java.net.http;

    exports ScanHub.GUI.controllers;
    opens ScanHub.GUI.controllers to javafx.fxml;
    exports ScanHub;
    opens ScanHub to javafx.fxml;
    exports ScanHub.DAL.interfaces;
    opens ScanHub.DAL.interfaces to javafx.fxml;
    exports ScanHub.BLL.interfaces;
    opens ScanHub.BLL.interfaces to javafx.fxml;
}