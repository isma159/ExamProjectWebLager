package ScanHub.GUI.controllers;

import ScanHub.BE.Role;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.UserTableRow;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private VBox userTableBox;

    private ModelFacade modelFacade;

    public AdminDashboardController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<User> users = modelFacade.userModel.getUsers();

            for (User user : users) {
                userTableBox.getChildren().add(UserTableRow.addRow(user));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}