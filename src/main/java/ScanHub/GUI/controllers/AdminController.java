package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private ToggleGroup sidebarBtns;
    @FXML private ToggleButton dashboardBtn, analyticsBtn, usersBtn, profilesBtn, metadataBtn, logsBtn, settingsBtn, shortcutsBtn;

    private ModelFacade modelFacade;

    public AdminController() throws Exception {
    }

    public void setModel(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
        sidebarBtns.selectToggle(dashboardBtn);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarBtns.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == dashboardBtn) {
                loadPage("/views/AdminDashboardView.fxml");
            } else if (newValue == usersBtn) {
                loadPage("/views/AdminUsersView.fxml");
            } else if (newValue == profilesBtn) {
                loadPage("/views/AdminProfilesView.fxml");
            }
        });
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AdminDashboardController.class) {
                    return new AdminDashboardController(modelFacade);
                } else if (controllerClass == AdminUsersController.class) {
                    return new AdminUsersController(modelFacade);
                } else if (controllerClass == AdminProfilesController.class) {
                    return new AdminProfilesController(modelFacade);
                }

                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Node page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}