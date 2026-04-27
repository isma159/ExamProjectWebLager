package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private TableView<User> tblUser;
    @FXML private TableView<Profile> tblProfile;
    @FXML private StackPane contentArea;

    @FXML private ToggleGroup sidebarBtns;
    @FXML private ToggleButton dashboardBtn, analyticsBtn, usersBtn, profilesBtn, metadataBtn, logsBtn, settingsBtn, shortcutsBtn;


    private ModelFacade modelFacade;

    public AdminController() throws Exception {
    }

    public void setModel(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        sidebarBtns.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == dashboardBtn) {
                loadPage("/views/AdminDashboardView.fxml");
            }
            else if (newValue == usersBtn) {
                loadPage("/views/AdminUsersView.fxml");
            }
        });

        dashboardBtn.fire();

    }

    // ----- CREATE & EDIT USER -----
    @FXML
    private void onClickCreateUser(ActionEvent actionEvent) throws IOException {
        openUserForm(null);
    }

    /**
     * Opens the UserFormView. If user is null, opens in create mode.
     * If user is provided (selected), opens in edit mode with fields prefilled.
     */
    private void openUserForm(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserFormView.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();

        UserFormController controller = loader.getController();
        controller.setModel(stage, modelFacade, user); // user is null for create, non-null for edit

        if (user == null) {
            stage.setTitle("Create User");
        } else {
            stage.setTitle("Edit User");
        }
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // ----- CREATE & UPDATE PROFILE -----
    @FXML
    private void onClickCreateProfile(ActionEvent actionEvent) throws IOException {
        openProfileForm(null);
    }

    @FXML
    private void onClickUpdateProfile(ActionEvent actionEvent) throws IOException {
        Profile selectedProfile = tblProfile.getSelectionModel().getSelectedItem();
        if (selectedProfile == null) return;
        openProfileForm(selectedProfile);
    }

    /**
     * Opens the ProfileFormView. If profile is null, opens in create mode.
     * If profile is provided (selected), opens in edit mode with fields prefilled.
     */
    private void openProfileForm(Profile profile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProfileFormView.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();

        ProfileFormController controller = loader.getController();
        controller.setModel(stage, modelFacade, profile); // profile is null for create, non-null for edit

        if (profile == null) {
            stage.setTitle("Create Profile");
        } else {
            stage.setTitle("Edit Profile");
        }
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void loadPage(String fxml) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AdminDashboardController.class) {
                    return new AdminDashboardController(modelFacade);
                }
                else if (controllerClass == AdminUsersController.class) {
                    return new AdminUsersController(modelFacade);
                }

                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Node page = loader.load();
            contentArea.getChildren().setAll(page);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}