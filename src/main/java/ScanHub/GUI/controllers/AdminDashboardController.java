package ScanHub.GUI.controllers;

// project imports
import ScanHub.BE.*;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;

// java imports
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private VBox userTableBox, profileTableBox, logTableBox;
    @FXML private Pagination pgUsers, pgProfiles, pgLogs;

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

    private List<Log> currentLogs = new ArrayList<>();
    private Stage currentStage;

    private final int TOTAL_TABLE_SIZE = 6;

    public AdminDashboardController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUsers();
        loadProfiles();
        loadLogs();

        pgUsers.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> {
            loadUsers();
        }));

        pgProfiles.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> {
            loadProfiles();
        }));

        pgLogs.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> {
            loadLogs();
        }));
    }

    private void loadUsers() {
        userTableBox.getChildren().clear();
        try {
            List<User> users = modelFacade.getUserModel().getUsers();

            pgUsers.setPageCount(Math.ceilDiv(users.size(), TOTAL_TABLE_SIZE));

            int startIndex = pgUsers.getCurrentPageIndex() * TOTAL_TABLE_SIZE;
            int endIndex = Math.min(startIndex + TOTAL_TABLE_SIZE, users.size());

            currentUsers = new ArrayList<>(users.subList(startIndex, endIndex));
            for (User user : currentUsers) {
                HBox row = RowMaker.addUserRow(user);
                row.setUserData(user);
                userTableBox.getChildren().add(row);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfiles() {
        profileTableBox.getChildren().clear();
        try {
            List<Profile> profiles = modelFacade.getProfileModel().getProfiles();

            pgProfiles.setPageCount(Math.ceilDiv(profiles.size(), TOTAL_TABLE_SIZE));

            int startIndex = pgProfiles.getCurrentPageIndex() * TOTAL_TABLE_SIZE;
            int endIndex = Math.min(startIndex + TOTAL_TABLE_SIZE, profiles.size());

            currentProfiles = new ArrayList<>(profiles.subList(startIndex, endIndex));
            for (Profile profile: currentProfiles) {
                HBox row = RowMaker.addProfileRow(profile);
                row.setUserData(profile);
                profileTableBox.getChildren().add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load dashboard data.");
        }
    }

    private void loadLogs() {
        logTableBox.getChildren().clear();
        try {
            List<Log> logs = modelFacade.getLogModel().getLogs();

            pgLogs.setPageCount(Math.ceilDiv(logs.size(), TOTAL_TABLE_SIZE));

            int startIndex = pgLogs.getCurrentPageIndex() * TOTAL_TABLE_SIZE;
            int endIndex = Math.min(startIndex + TOTAL_TABLE_SIZE, logs.size());

            currentLogs = new ArrayList<>(logs.subList(startIndex, endIndex));
            for (Log log: currentLogs) {
                HBox row = RowMaker.addLogRow(log);
                row.setUserData(log);
                logTableBox.getChildren().add(row);
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

    private void filterUsers() { // TODO: needs rework
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
        filterUsers();
    }

    @FXML
    private void onTbAllProfilesClick(){
        selectedStatus =  null;
        filterProfiles();
    }

    @FXML
    private void onTbActiveClick(){
        selectedStatus = ProfileStatus.ACTIVE;
        filterProfiles();
    }


    @FXML
    private void onTbInactiveClick()
    { selectedStatus = ProfileStatus.INACTIVE;
        filterProfiles();
    }

    private void filterProfiles() { // TODO: needs rework
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
        filterProfiles();
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
        filterProfiles();
    }
}
