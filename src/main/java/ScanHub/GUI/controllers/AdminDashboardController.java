package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private VBox userTableBox;
    @FXML private VBox profileTableBox;

    private ModelFacade modelFacade;

    public AdminDashboardController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<User> users = modelFacade.userModel.getUsers();
            List<Profile> profiles = modelFacade.profileModel.getProfiles();

            for (User user : users) {
                userTableBox.getChildren().add(RowMaker.addUserRow(user));
            }

            for (Profile profile: profiles) {
                profileTableBox.getChildren().add(RowMaker.addProfileRow(profile));
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load dashboard data.");
        }
    }
}
