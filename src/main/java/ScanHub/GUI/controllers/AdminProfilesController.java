package ScanHub.GUI.controllers;

// project imports
import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IShortcutHandler;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import ScanHub.GUI.util.TableLoader;
import ScanHub.GUI.util.ViewHandler;

// javafx imports
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// java imports
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class AdminProfilesController implements Initializable, IShortcutHandler {

    @FXML private VBox profileTableBox;
    @FXML private TextField txtFldSearchProfiles;
    @FXML private Pagination pgProfiles;

    private final ModelFacade modelFacade;
    private Stage currentStage;

    private List<Profile> currentProfiles = new ArrayList<>();
    private boolean profileAscending;
    private Profile selectedProfile = null;
    private ProfileStatus selectedStatus = null;
    private HBox selectedProfileRow;

    private final int TOTAL_TABLE_SIZE = 15;

    public AdminProfilesController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterProfiles();
        txtFldSearchProfiles.textProperty().addListener((observable, oldValue, newValue) -> filterProfiles());
        pgProfiles.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> filterProfiles()));
    }

    private void loadProfiles(List<Profile> profiles) {
        try {
            selectedProfile = null;
            selectedProfileRow = null;

            // sets up with all profiles by running a for-loop that makes an interactive HBox of every profile
            TableLoader.loadTable(profileTableBox, pgProfiles, TOTAL_TABLE_SIZE, profiles, item ->{
                Profile profile = (Profile) item;
                return RowMaker.addProfileRow(profile, this::selectProfile);
            });

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load profiles.");
        }
    }

    private void selectProfile(Profile profile, HBox rowHBox) {
        if (selectedProfileRow != null) {
            selectedProfileRow.getStyleClass().remove("row-selected");
            selectedProfile = null;
            selectedProfileRow = null;
        }

        selectedProfile = profile;
        selectedProfileRow = rowHBox;
        rowHBox.getStyleClass().add("row-selected");
    }


    @FXML
    private void onClickCreateProfile() {
        openProfileForm(null);
    }

    @FXML
    private void onClickUpdateProfile() {
        if (selectedProfile == null) {
            AlertHelper.showWarning("No Selection", "Please select a profile to edit.");
            return;
        }
        openProfileForm(selectedProfile);
    }

    @FXML
    private void onClickDeleteProfile(MouseEvent mouseEvent) {
        if (selectedProfile == null) {
            AlertHelper.showWarning("No Selection", "Please select a profile to delete.");
            return;
        }

        AlertHelper.showConfirmation("Delete Profile", "Are you sure you want to delete the profile \"" + selectedProfile.getProfileName() + "\"? This action cannot be undone.", () -> {
                    try {
                        modelFacade.getProfileModel().deleteProfile(selectedProfile);
                        modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), selectedProfile.getProfileId(), EntityType.PROFILE, LogAction.DELETE, LocalDateTime.now()));
                        modelFacade.getClientModel().refreshClients();
                        modelFacade.getUserModel().refreshUsers();
                        filterProfiles();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertHelper.showError("Delete Failed", "Failed to delete profile. Please try again.");
                    }
                }
        );
    }

    private void openProfileForm(Profile profile) {
        try {
            ViewHandler handler = profile == null ? ViewHandler.CREATE_PROFILE : ViewHandler.EDIT_PROFILE;
            handler.reset();
            handler.preLoad();
            ProfileFormController controller = handler.getController();
            Stage stage = handler.prepareStage();
            controller.setModel(stage, modelFacade, profile);

            stage.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                    event.consume();
                }
            });

            stage.showAndWait();

            filterProfiles(); // refresh the list after the form closes
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the profile form. Please try again.");
        }
    }

    private void filterProfiles() {

        String search = txtFldSearchProfiles.getText().toLowerCase();

        List<Profile> profiles = modelFacade.getProfileModel().getProfiles();

        profiles = profiles.stream().filter(p -> {

            boolean statusMatch = selectedStatus == null || selectedStatus == p.getStatus();
            boolean searchMatch = search.isBlank() || p.getProfileName().toLowerCase().contains(search);

            return statusMatch && searchMatch;
        }).toList();

        loadProfiles(profiles);
    }

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        return Map.of(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::onClickCreateProfile,
                new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), this::onClickUpdateProfile,
                new KeyCodeCombination(KeyCode.DELETE), () -> onClickDeleteProfile(null)
        );
    }

    @FXML
    private void onTbAllProfilesClick() {
        selectedStatus =  null;
        filterProfiles();
    }

    @FXML
    private void onTbActiveClick() {
        selectedStatus = ProfileStatus.ACTIVE;
        filterProfiles();
    }


    @FXML
    private void onTbInactiveClick() {
        selectedStatus = ProfileStatus.INACTIVE;
        filterProfiles();
    }

    @FXML
    private void onProfileNameClick() {
        // toggle ascending and descending order
        profileAscending = !profileAscending;
        // sorting the profile names on the direction
        currentProfiles.sort(profileAscending ? Comparator.comparing(Profile::getProfileName) : Comparator.comparing(Profile::getProfileName).reversed());
        profileTableBox.getChildren().clear();

        for (Profile profile : currentProfiles) {
            HBox row = RowMaker.addProfileRow(profile, (clickedProfile, rowHBox) -> {
                if (selectedProfile != null) selectedProfileRow.getStyleClass().remove("row-selected");
                if (selectedProfile == clickedProfile) { selectedProfile = null; selectedProfileRow = null; return;}
                selectedProfile = clickedProfile;
                selectedProfileRow = rowHBox;
                rowHBox.getStyleClass().add("row-selected");
            });
            row.setFocusTraversable(true);
            row.focusedProperty().addListener((observable, oldValue, isFocused) -> {
                if (isFocused) {
                    selectProfile(profile, row);
                }
            });
            row.setUserData(profile);
            profileTableBox.getChildren().add(row);
        }
        filterProfiles();
    }
}
