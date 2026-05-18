package ScanHub.GUI.controllers;

// project imports

import ScanHub.BE.Client;
import ScanHub.BE.Log;
import ScanHub.BE.User;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.Role;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ClientFormController implements Initializable {

    @FXML private Label formTitle;
    @FXML private TextField clientNameField;
    @FXML private Button saveButton;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private Client editingClient = null;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }


    public void setModel(Stage currentStage, ModelFacade modelFacade, Client client) {
        this.currentStage = currentStage;
        this.modelFacade = modelFacade;
        this.editingClient = client;

        if (editingClient != null) {
            formTitle.setText("Edit client");
            saveButton.setText("Save Changes");
            populateFields(editingClient);
        }

        ThemeManager.apply(currentStage.getScene());
    }

    /**
     * Pre-fills input fields when editing an existing client.
     * TODO: populate profile checkboxes from user's assigned profiles
     */
    private void populateFields(Client client) {
        clientNameField.setText(client.getClientName());
    }

    @FXML
    private void onClickSave(ActionEvent actionEvent) {
        if (editingClient != null) {
            updateClient();
        } else {
            createClient();
        }

        try {
            modelFacade.getUserModel().refreshUsers();
            modelFacade.getProfileModel().refreshProfiles();
            modelFacade.getClientModel().refreshClients();
        }
        catch (Exception e) {
            AlertHelper.showError("Refreshing users", "Could not refresh list of users. Please try saving again.");
        }
    }

    private void createClient() {
        String clientName = clientNameField.getText();

        clearError(); // prevents stacking of error borders

        if (clientName.isBlank()) {
            clientNameField.getStyleClass().add("error-border");

            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields and select a role.");
            return;
        }

        try {
            Client newClient = new Client(clientName);
            modelFacade.getClientModel().createClient(newClient);
            modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), newClient.getClientId(), EntityType.CLIENT, LogAction.CREATE, LocalDateTime.now()));
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Create Failed", "Failed to create user. Please try again.");
        }
    }

    /**
     * TODO find out if a user should be be able to be updated to admin and vice versa
     */
    private void updateClient() {
        String newClientName = clientNameField.getText();

        clearError(); // prevents stacking of error borders

        if (newClientName.isBlank()) {
            clientNameField.getStyleClass().add("error-border");

            AlertHelper.showWarning("Missing Fields", "Please fill in username and select a role.");
            return;
        }

        editingClient.setClientName(newClientName);

        try {
            modelFacade.getClientModel().updateClient(editingClient);
            modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), editingClient.getClientId(), EntityType.CLIENT, LogAction.UPDATE, LocalDateTime.now()));
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Update Failed", "Failed to update user. Please try again.");
        }
    }

    private void clearError() {
        clientNameField.getStyleClass().remove("error-border");
    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }
}
