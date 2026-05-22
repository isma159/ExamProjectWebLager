package ScanHub.GUI.controllers;

// project imports
import ScanHub.GUI.interfaces.IShortcutHandler;
import ScanHub.GUI.util.GlobalKeyHandler;
import ScanHub.GUI.util.ThemeHandler;
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
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.controlsfx.control.ToggleSwitch;

public class AdminController implements IViewController, Initializable {

    @FXML private StackPane contentArea;
    @FXML private ToggleGroup sidebarBtns;
    @FXML private ToggleButton dashboardBtn, usersBtn, clientsBtn, profilesBtn, metadataBtn, logsBtn, shortcutsBtn;
    @FXML private ToggleSwitch darkMode;
    @FXML private Label lblUsername, lblRole;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private final Map<KeyCodeCombination, Runnable> adminShortcuts = new HashMap<>();

    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;

        sidebarBtns.selectToggle(dashboardBtn);
        lblUsername.setText(modelFacade.getSessionModel().getCurrentUser().getUsername());
        lblRole.setText(modelFacade.getSessionModel().getCurrentUser().getRole().toString());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                () -> {sidebarBtns.selectToggle(dashboardBtn); contentArea.requestFocus();}
        );
        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN),
                () -> {sidebarBtns.selectToggle(usersBtn); contentArea.requestFocus();}
        );
        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
                () -> {sidebarBtns.selectToggle(clientsBtn); contentArea.requestFocus();}
        );
        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
                () -> {sidebarBtns.selectToggle(profilesBtn); contentArea.requestFocus();}
        );
        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN),
                () -> {sidebarBtns.selectToggle(metadataBtn); contentArea.requestFocus();}
        );
        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
                () -> {sidebarBtns.selectToggle(logsBtn); contentArea.requestFocus();}
        );
        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
                () -> {sidebarBtns.selectToggle(shortcutsBtn); contentArea.requestFocus();}
        );
        adminShortcuts.put(
                new KeyCodeCombination(KeyCode.F2),
                () -> {darkMode.setSelected(!darkMode.isSelected()); ThemeHandler.toggle(contentArea.getScene(), darkMode.isSelected());}
        );

        sidebarBtns.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                oldValue.setSelected(true);
            }

            if (newValue == dashboardBtn) {
                loadPage("/views/AdminDashboardView.fxml");
            } else if (newValue == usersBtn) {
                loadPage("/views/AdminUsersView.fxml");
            } else if (newValue == clientsBtn) {
                loadPage("/views/AdminClientsView.fxml");
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

        // Push admin shortcuts as the persistent global layer so they are
        // always active regardless of which sub-page is loaded.
        GlobalKeyHandler.getInstance().setLayer(adminShortcuts);
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AdminDashboardController.class) {
                    return new AdminDashboardController(modelFacade, currentStage);
                } else if (controllerClass == AdminUsersController.class) {
                    return new AdminUsersController(modelFacade, currentStage);
                } else if (controllerClass == AdminClientsController.class) {
                    return new AdminClientsController(modelFacade, currentStage);
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

            IShortcutHandler controller = loader.getController();
            // Merge admin-level + page-level shortcuts into GlobalKeyHandler
            Map<KeyCodeCombination, Runnable> merged = new HashMap<>(adminShortcuts);
            merged.putAll(controller.getShortcuts());

            GlobalKeyHandler.getInstance().setLayer(merged);

            if (darkMode.isSelected()) {
                page.getStyleClass().add("dark");
            }
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error", "Failed to load the selected page. Please try again.");
        }
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
        ThemeHandler.toggle(contentArea.getScene(), darkMode.isSelected());
    }

    public void onClickOpenScanView() {
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