package ScanHub.GUI.controllers;

// project imports
import ScanHub.BE.*;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.enums.Role;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IShortcutHandler;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;

// java imports
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.util.*;

public class AdminDashboardController implements Initializable, IShortcutHandler {

    @FXML private VBox userTableBox, profileTableBox, logTableBox;
    @FXML private Pagination pgUsers, pgProfiles, pgLogs;
    @FXML private SearchableComboBox<LogAction> cbFilter;

    private boolean userAscending;
    private boolean profileAscending;
    private final ModelFacade modelFacade;

    private List<User> currentUsers =  new ArrayList<>();
    private Role selectedRole = null;

    private List<Profile> currentProfiles =  new ArrayList<>();
    private Profile selectedProfile = null;
    private HBox selectedProfileRow = null;
    private ProfileStatus selectedStatus = null;

    private List<Log> currentLogs = new ArrayList<>();
    private LogAction selectedAction = LogAction.ALL;

    private final Stage currentStage;

    private final int TOTAL_TABLE_SIZE = 6;

    public AdminDashboardController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterUsers();
        filterProfiles();
        filterLogs();

        pgUsers.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> { filterUsers(); }));
        pgProfiles.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> { filterProfiles(); }));
        pgLogs.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> { filterLogs(); }));

        cbFilter.getItems().addAll(LogAction.values());
        cbFilter.getSelectionModel().select(LogAction.ALL);

        cbFilter.valueProperty().addListener(((observable, oldValue, newValue) -> {selectedAction = newValue; filterLogs();}));
    }

    private void loadUsers(List<User> users) {
        userTableBox.getChildren().clear();
        try {
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

    private void loadProfiles(List<Profile> profiles) {
        profileTableBox.getChildren().clear();
        try {

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

    private void loadLogs(List<Log> logs) {
        logTableBox.getChildren().clear();
        try {

            pgLogs.setPageCount(Math.ceilDiv(logs.size(), TOTAL_TABLE_SIZE));

            int startIndex = pgLogs.getCurrentPageIndex() * TOTAL_TABLE_SIZE;
            int endIndex = Math.min(startIndex + TOTAL_TABLE_SIZE, logs.size());

            List<Log> currentLogs = new ArrayList<>(logs.subList(startIndex, endIndex));
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

    private void filterUsers() {
        List<User> users = modelFacade.getUserModel().getUsers();

        users = users.stream().filter(user -> {
            if (selectedRole == null) {return true;}
            return user.getRole() == selectedRole;
        }).toList();

        loadUsers(users);

    }

    @FXML
    private void onUsernameClick(Event event) {
        // toggle ascending and descending order
        userAscending = !userAscending;
        // sorting the usernames on the direction
        currentUsers.sort(userAscending ? Comparator.comparing(User::getUsername) : Comparator.comparing(User::getUsername).reversed());
        userTableBox.getChildren().clear();

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

    private void filterProfiles() {
        List<Profile> profiles = modelFacade.getProfileModel().getProfiles();

        profiles = profiles.stream().filter(profile -> {
            if (selectedStatus == null) {return true;}
            return profile.getStatus() == selectedStatus;
        }).toList();

        loadProfiles(profiles);
    }

    private void filterLogs() {
        List<Log> logs = modelFacade.getLogModel().getLogs();

        logs = logs.stream().filter(log -> {
            if (selectedAction == LogAction.ALL) {return true;}
            return log.getAction() == selectedAction;
        }).toList();



        loadLogs(logs);
    }

    @FXML
    private void onProfileNameClick() {
        profileAscending = !profileAscending;
        currentProfiles.sort(profileAscending ? Comparator.comparing(Profile::getProfileName) : Comparator.comparing(Profile::getProfileName).reversed());
        filterProfiles();
    }

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        return Map.of();
    }
}
