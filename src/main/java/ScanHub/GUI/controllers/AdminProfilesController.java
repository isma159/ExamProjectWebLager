package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.ProfileStatus;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminProfilesController implements Initializable {

    @FXML private VBox profileTableBox;
    @FXML private TextField txtFldSearchProfiles;
    private List<Profile> currentProfiles = new ArrayList<>();
    private boolean ascending;
    private ModelFacade modelFacade;
    private Profile selectedProfile = null;
    private ProfileStatus selectedStatus = null;
    private HBox selectedRow;

    public AdminProfilesController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfiles();
        txtFldSearchProfiles.textProperty().addListener((observable, oldValue, newValue) -> filterProfiles(newValue));
    }

    private void loadProfiles() {
        try {
            // resets
            profileTableBox.getChildren().clear();
            selectedProfile = null;
            selectedRow = null;

            // sets up with all profiles by running a for-loop that makes an interactive HBox of every profile
            List<Profile> profiles = modelFacade.profileModel.getProfiles();
            currentProfiles = new ArrayList<>(profiles);
            for (Profile profile : currentProfiles) {
                HBox row = RowMaker.addProfileRow(profile, (clickedProfile, rowHBox) -> {
                    // clear highlight of previously selected row
                    if (selectedRow != null) {
                        selectedRow.getStyleClass().remove("row-selected");
                    }
                    // remove selected row if clicked on again
                    if (selectedProfile == clickedProfile) {
                        selectedProfile = null;
                        selectedRow = null;
                        return; // will reselect on the next line if not returning
                    }
                    // select clicked row as selected profile (with highlight to show)
                    selectedProfile = clickedProfile;
                    selectedRow = rowHBox;
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
                        modelFacade.profileModel.deleteProfile(selectedProfile);
                        loadProfiles();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertHelper.showError("Delete Failed", "Failed to delete profile. Please try again.");
                    }
                }
        );
    }

    private void openProfileForm(Profile profile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProfileFormView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();

            stage.setTitle(profile == null ? "Create Profile" : "Edit Profile");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            ProfileFormController controller = loader.getController();
            controller.setModel(stage, modelFacade, profile);

            stage.showAndWait();

            // refresh the list after the form closes
            loadProfiles();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the profile form. Please try again.");
        }
    }

    private void filterProfiles(String search) {
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
        selectedStatus = ProfileStatus.Active;
        filterProfiles(txtFldSearchProfiles.getText());
    }


    @FXML
    private void onTbInactiveClick()
    { selectedStatus = ProfileStatus.Inactive;
        filterProfiles(txtFldSearchProfiles.getText());
    }

    @FXML
    private void onProfileNameClick() {
        // toggle ascending and descending order
        ascending = !ascending;
        // sorting the profile names on the direction
        currentProfiles.sort(ascending ? Comparator.comparing(Profile::getProfileName) : Comparator.comparing(Profile::getProfileName).reversed());
        profileTableBox.getChildren().clear();

        for (Profile profile : currentProfiles) {
            HBox row = RowMaker.addProfileRow(profile, (clickedProfile, rowHBox) -> {
                if (selectedProfile != null) selectedRow.getStyleClass().remove("row-selected");
                if (selectedProfile == clickedProfile) { selectedProfile = null; selectedRow = null; return;}
                selectedProfile = clickedProfile;
                selectedRow = rowHBox;
                rowHBox.getStyleClass().add("row-selected");
            });
            row.setUserData(profile);
            profileTableBox.getChildren().add(row);
        }
    }

    @FXML
    private void onSplitBehaviorClick() {
        // toggle ascending and descending order
        ascending = !ascending;
        // sorting the profile names on the direction
        currentProfiles.sort(ascending ? Comparator.comparing(Profile::getProfileName) : Comparator.comparing(Profile::getProfileName).reversed());
        profileTableBox.getChildren().clear();

        for (Profile profile : currentProfiles) {
            HBox row = RowMaker.addProfileRow(profile, (clickedProfile, rowHBox) -> {
                if (selectedProfile != null) selectedRow.getStyleClass().remove("row-selected");
                if (selectedProfile == clickedProfile) { selectedProfile = null; selectedRow = null; return;}
                selectedProfile = clickedProfile;
                selectedRow = rowHBox;
                rowHBox.getStyleClass().add("row-selected");
            });
            row.setUserData(profile);
            profileTableBox.getChildren().add(row);
        }
    }
}
