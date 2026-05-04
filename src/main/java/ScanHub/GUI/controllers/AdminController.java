package ScanHub.GUI.controllers;

import ScanHub.BLL.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements IViewController, Initializable {

    @FXML private StackPane contentArea;
    @FXML private ToggleGroup sidebarBtns;
    @FXML private ToggleButton dashboardBtn, analyticsBtn, usersBtn, profilesBtn, metadataBtn, logsBtn, settingsBtn, shortcutsBtn;
    @FXML private HBox analyticsBox, metadataBox, systemBox, logBox, settingsBox, shortcutBox;
    @FXML private ToggleSwitch darkMode;

    private Stage currentStage;
    private ModelFacade modelFacade;

    public AdminController() throws Exception {
    }

    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
        sidebarBtns.selectToggle(dashboardBtn);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarBtns.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                oldValue.setSelected(true);
            }

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
            AlertHelper.showError("Navigation Error", "Failed to load the selected page. Please try again.");
        }
    }

    @FXML
    private void onClickLogOut(ActionEvent actionEvent) { // TODO
        AlertHelper.showConfirmation("Log Out", "Are you sure you want to log out?", () -> {
            try {
                ViewHandler handler = ViewHandler.LOGIN;
                handler.reset();
                handler.show(modelFacade);
                currentStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Logout Error", "Failed to log out. Please try again.");
            }
        });
    }

    @FXML
    private void onDarkModeToggle() {
        ThemeManager.toggle(contentArea.getScene(), darkMode.isSelected());
    }

    public void onClickOpenScanView(MouseEvent mouseEvent) {
        try {
            ViewHandler handler = ViewHandler.SCAN_VIEW;
            handler.reset();
            Stage stage = new Stage();
            handler.show(modelFacade, stage);
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Scan Workshop Error", "Failed to open Scan Workshop. Please try again.");
        }
    }
}
