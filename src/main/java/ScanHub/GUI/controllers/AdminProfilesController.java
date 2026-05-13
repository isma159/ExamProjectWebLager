package ScanHub.GUI.controllers;

// project imports
import ScanHub.BE.*;
import ScanHub.BLL.SessionManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;

// java imports
import ScanHub.GUI.util.ViewHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminProfilesController implements Initializable {

    @FXML private VBox profileTableBox;
    @FXML private TextField txtFldSearchProfiles;
    @FXML private Pagination pgProfiles;
    private List<Profile> currentProfiles = new ArrayList<>();
    private boolean profileAscending;
    private ModelFacade modelFacade;
    private Profile selectedProfile = null;
    private ProfileStatus selectedStatus = null;
    private HBox selectedProfileRow;
    private SessionManager sessionManager = SessionManager.getInstance();
    private Stage currentStage;

    private final int TOTAL_TABLE_SIZE = 15;

    public AdminProfilesController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfiles();
        txtFldSearchProfiles.textProperty().addListener((observable, oldValue, newValue) -> filterProfiles(newValue));
        pgProfiles.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> {
            loadProfiles();
        }));
    }

    private void loadProfiles() {
        try {
            // resets
            profileTableBox.getChildren().clear();
            selectedProfile = null;
            selectedProfileRow = null;

            // sets up with all profiles by running a for-loop that makes an interactive HBox of every profile
            List<Profile> profiles = modelFacade.getProfileModel().getProfiles();

            pgProfiles.setPageCount(Math.ceilDiv(profiles.size(), TOTAL_TABLE_SIZE));

            int startIndex = pgProfiles.getCurrentPageIndex() * TOTAL_TABLE_SIZE;
            int endIndex = Math.min(startIndex + TOTAL_TABLE_SIZE, profiles.size());

            currentProfiles = new ArrayList<>(profiles.subList(startIndex, endIndex));
            for (Profile profile : currentProfiles) {
                HBox row = RowMaker.addProfileRow(profile, (clickedProfile, rowHBox) -> {
                    // clear highlight of previously selected row
                    if (selectedProfileRow != null) {
                        selectedProfileRow.getStyleClass().remove("row-selected");
                    }
                    // remove selected row if clicked on again
                    if (selectedProfile == clickedProfile) {
                        selectedProfile = null;
                        selectedProfileRow = null;
                        return; // will reselect on the next line if not returning
                    }
                    // select clicked row as selected profile (with highlight to show)
                    selectedProfile = clickedProfile;
                    selectedProfileRow = rowHBox;
                    rowHBox.getStyleClass().add("row-selected");
                });
                row.setUserData(profile);
                profileTableBox.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load profiles.");
        }
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
                        modelFacade.getLogModel().createLog(new Log(sessionManager.getCurrentUser(), selectedProfile.getProfileId(), EntityType.PROFILE, LogAction.DELETE, LocalDateTime.now()));
                        loadProfiles();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertHelper.showError("Delete Failed", "Failed to delete profile. Please try again.");
                    }
                }
        );
    }

    private void openProfileForm(Profile profile) { // TODO
        try {
            ViewHandler handler = profile == null ? ViewHandler.CREATE_PROFILE : ViewHandler.EDIT_PROFILE;
            handler.reset();
            handler.preLoad();
            ProfileFormController controller = handler.getController();
            Stage stage = handler.prepareStage();
            controller.setModel(stage, modelFacade, profile);
            stage.showAndWait();

            loadProfiles(); // refresh the list after the form closes
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the profile form. Please try again.");
        }
    }

    private void filterProfiles(String search) { // TODO: needs rework
        for (var node : profileTableBox.getChildren()) {
            HBox row = (HBox) node;
            Profile profile = (Profile) row.getUserData();

            boolean matching = search.isBlank()
            || profile.getProfileName().toLowerCase().contains(search.toLowerCase()) || profile.getSplitBehavior().name().toLowerCase().contains(search.toLowerCase());
            boolean matchingStatus = selectedStatus == null || profile.getStatus() == selectedStatus;
            row.setVisible(matching && matchingStatus);
            row.setManaged(matching && matchingStatus);
        }
    }

    @FXML
    private void onTbAllProfilesClick(){
    selectedStatus =  null;
    filterProfiles(txtFldSearchProfiles.getText());
    }

    @FXML
    private void onTbActiveClick(){
        selectedStatus = ProfileStatus.ACTIVE;
        filterProfiles(txtFldSearchProfiles.getText());
    }


    @FXML
    private void onTbInactiveClick()
    { selectedStatus = ProfileStatus.INACTIVE;
        filterProfiles(txtFldSearchProfiles.getText());
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
            row.setUserData(profile);
            profileTableBox.getChildren().add(row);
        }
        filterProfiles(txtFldSearchProfiles.getText());
    }

    @FXML
    private void onSplitBehaviorClick() {
        // toggle ascending and descending order
        profileAscending = !profileAscending;

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
            row.setUserData(profile);
            profileTableBox.getChildren().add(row);
        }
        filterProfiles(txtFldSearchProfiles.getText());
    }
}
