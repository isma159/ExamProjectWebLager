package ScanHub.GUI.controllers;

// project imports
import ScanHub.GUI.util.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;

// java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import org.controlsfx.control.ToggleSwitch;

public class AdminController implements IViewController, Initializable {

    @FXML private StackPane contentArea;
    @FXML private ToggleGroup sidebarBtns;
    @FXML private ToggleButton dashboardBtn, analyticsBtn, usersBtn, profilesBtn, metadataBtn, logsBtn, settingsBtn, shortcutsBtn;
    @FXML private HBox analyticsBox, metadataBox, systemBox, logBox, settingsBox, shortcutBox;
    @FXML private ToggleSwitch darkMode;
    @FXML private Label lblUsername, lblRole;

    private Stage currentStage;
    private ModelFacade modelFacade;

    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;

        sidebarBtns.selectToggle(dashboardBtn);
        lblUsername.setText(modelFacade.getSessionModel().getCurrentUser().getUsername());
        lblRole.setText(modelFacade.getSessionModel().getCurrentUser().getRole().toString());
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
            } else if (newValue == metadataBtn) {
                loadPage("/views/AdminMetadataView.fxml");
            } else if (newValue == logsBtn) {
                loadPage("/views/AdminLogsView.fxml");
            } else if (newValue == shortcutsBtn)  {
                loadPage("/views/ShortcutsView.fxml");
            }
        });

        javafx.application.Platform.runLater(this::registerShortcuts);
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AdminDashboardController.class) {
                    return new AdminDashboardController(modelFacade, currentStage);
                } else if (controllerClass == AdminUsersController.class) {
                    return new AdminUsersController(modelFacade, currentStage);
                } else if (controllerClass == AdminProfilesController.class) {
                    return new AdminProfilesController(modelFacade, currentStage);
                } else if (controllerClass == AdminMetadataController.class) {
                    return new AdminMetadataController(modelFacade, currentStage);
                } else if (controllerClass == AdminLogsController.class) {
                    return new AdminLogsController(modelFacade, currentStage);
                } else if (controllerClass == ShortcutsController.class) {
                    return new ShortcutsController(modelFacade, currentStage);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Node page = loader.load();
            if (darkMode.isSelected()) {
                page.getStyleClass().add("dark");
            }
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error", "Failed to load the selected page. Please try again.");
        }
    }

    private void registerShortcuts() {
        Scene scene = contentArea.getScene();
        if (scene == null) {
            return;
        }

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case D -> {
                    if (event.isControlDown()) {
                        sidebarBtns.selectToggle(dashboardBtn);
                        contentArea.requestFocus();
                    }
                }
                case U -> {
                    if (event.isControlDown()) {
                        sidebarBtns.selectToggle(usersBtn);
                        contentArea.requestFocus();
                    }
                }
                case P -> {
                    if (event.isControlDown()) {
                        sidebarBtns.selectToggle(profilesBtn);
                        contentArea.requestFocus();
                    }
                }
                case M -> {
                    if (event.isControlDown()) {
                        sidebarBtns.selectToggle(metadataBtn);
                        contentArea.requestFocus();
                    }
                }
                case L -> {
                    if (event.isControlDown()) {
                        sidebarBtns.selectToggle(logsBtn);
                        contentArea.requestFocus();
                    }
                }
                case A -> {
                    if (event.isControlDown()) {
                        sidebarBtns.selectToggle(analyticsBtn);
                        contentArea.requestFocus();
                    }
                }
                case H -> {
                    if (event.isControlDown()) {
                        sidebarBtns.selectToggle(shortcutsBtn);
                        contentArea.requestFocus();
                    }
                }
                case F2 -> {
                    darkMode.setSelected(!darkMode.isSelected());
                    ThemeManager.toggle(contentArea.getScene(), darkMode.isSelected());
                }
            }
        });
    }

    @FXML
    private void onClickLogOut(ActionEvent actionEvent) {
        AlertHelper.showConfirmation("Log Out", "Are you sure you want to log out?", () -> {
            try {
                ViewHandler handler = ViewHandler.LOGIN;
                handler.reset();
                handler.show(modelFacade);
                modelFacade.getSessionModel().logout();
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
            handler.show(modelFacade).setMaximized(true);
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Scan Workshop Error", "Failed to open Scan Workshop. Please try again.");
        }
    }
}
