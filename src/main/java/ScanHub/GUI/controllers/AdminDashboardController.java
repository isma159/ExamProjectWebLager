package ScanHub.GUI.controllers;

// project imports
import ScanHub.BE.Profile;
import ScanHub.BE.ProfileStatus;
import ScanHub.BE.Role;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;

// java imports
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private VBox userTableBox;
    @FXML private VBox profileTableBox;

    private boolean userAscending;
    private boolean profileAscending;
    private ModelFacade modelFacade;

    private List<User> currentUsers =  new ArrayList<>();
    private User selectedUser = null;
    private HBox selectedUserRow = null;
    private Role selectedRole = null;

    private List<Profile> currentProfiles =  new ArrayList<>();
    private Profile selectedProfile = null;
    private HBox selectedProfileRow = null;
    private ProfileStatus selectedStatus = null;


    public AdminDashboardController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<User> users = modelFacade.userModel.getUsers();
            List<Profile> profiles = modelFacade.profileModel.getProfiles();

            currentUsers = new ArrayList<>(users);
            for (User user : users) {
                HBox row = RowMaker.addUserRow(user);
                row.setUserData(user);
                userTableBox.getChildren().add(row);
            }

            currentProfiles = new ArrayList<>(profiles);
            for (Profile profile: profiles) {
                HBox row = RowMaker.addProfileRow(profile);
                row.setUserData(profile);
                profileTableBox.getChildren().add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load dashboard data.");
        }
    }

    @FXML
    private void onTbAllUsersClick(ActionEvent actionEvent) {
        selectedRole = null;
        filterUsers();
    }

    @FXML
    private void onTbAdminsClick(ActionEvent actionEvent) {
        selectedRole = Role.ADMIN;
        filterUsers();
    }

    @FXML
    private void onTbUsersClick(ActionEvent actionEvent) {
        selectedRole = Role.USER;
        filterUsers();
    }

    private void filterUsers() {
        for (var node : userTableBox.getChildren()) {
            HBox row = (HBox) node;
            User user = (User) row.getUserData();
            boolean matchingRole = selectedRole ==  null || user.getRole() == selectedRole;

            row.setVisible(matchingRole);
            row.setManaged(matchingRole);
        }
    }

    @FXML
    private void onUsernameClick(Event event) {
        // toggle ascending and descending order
        userAscending = !userAscending;
        // sorting the usernames on the direction
        currentUsers.sort(userAscending ? Comparator.comparing(User::getUsername) : Comparator.comparing(User::getUsername).reversed());
        userTableBox.getChildren().clear();

        for (User user : currentUsers) {
            HBox row = RowMaker.addUserRow(user, (clickedUser, rowHBox) -> {
                if (selectedUser != null) selectedUserRow.getStyleClass().remove("row-selected");
                if (selectedUser == clickedUser) { selectedUser = null; selectedUserRow = null; return;}
                selectedUser = clickedUser;
                selectedUserRow = rowHBox;
                rowHBox.getStyleClass().add("row-selected");
            });
            row.setUserData(user);
            userTableBox.getChildren().add(row);
        }
    }

    @FXML
    private void onTbAllProfilesClick(){
        selectedStatus =  null;
        filterProfiles();
    }

    @FXML
    private void onTbActiveClick(){
        selectedStatus = ProfileStatus.Active;
        filterProfiles();
    }


    @FXML
    private void onTbInactiveClick()
    { selectedStatus = ProfileStatus.Inactive;
        filterProfiles();
    }

    private void filterProfiles() {
        for (var node : profileTableBox.getChildren()) {
            HBox row = (HBox) node;
            Profile profile = (Profile) row.getUserData();

            boolean matchingStatus = selectedStatus == null || profile.getStatus() == selectedStatus;
            row.setVisible(matchingStatus);
            row.setManaged(matchingStatus);
        }
    }

    @FXML
    private void onProfileNameClick() {
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
    }

    @FXML
    private void onSplitBehaviorClick() {
        profileAscending = !profileAscending;
        currentProfiles.sort(profileAscending ? Comparator.comparing(Profile::getProfileName) : Comparator.comparing(Profile::getProfileName).reversed());
        profileTableBox.getChildren().clear();

        for (Profile profile : currentProfiles) {
            HBox row = RowMaker.addProfileRow(profile, (clickedProfile, rowHBox) -> {
                if (selectedProfile != null) selectedProfileRow.getStyleClass().remove("row-selected");
                if (selectedProfile == clickedProfile) {
                    selectedProfile = null;
                    selectedProfileRow = null;
                    return;
                }
                selectedProfile = clickedProfile;
                selectedProfileRow = rowHBox;
                rowHBox.getStyleClass().add("row-selected");
            });
            row.setUserData(profile);
            profileTableBox.getChildren().add(row);
        }
    }
}
