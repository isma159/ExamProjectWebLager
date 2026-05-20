package ScanHub.GUI.controllers;

import ScanHub.BE.Client;
import ScanHub.BE.Log;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IShortcutHandler;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import ScanHub.GUI.util.TableLoader;
import ScanHub.GUI.util.ViewHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminClientsController implements Initializable, IShortcutHandler {

    @FXML private Pagination pgClients;
    @FXML private VBox clientTableBox;

    private ModelFacade modelFacade;
    private Stage currentStage;
    private Client selectedClient;
    private HBox selectedClientRow;

    private final int TOTAL_TABLE_SIZE = 15;

    public AdminClientsController(ModelFacade modelFacade, Stage currentStage) {

        this.modelFacade = modelFacade;
        this.currentStage = currentStage;

    }

    private void loadClients() {
        try {
            selectedClient = null;
            selectedClientRow = null;

            List<Client> clients = modelFacade.getClientModel().getClients();
            TableLoader.loadTable(clientTableBox, pgClients, TOTAL_TABLE_SIZE, clients, item -> {
                Client client = (Client) item;
                return RowMaker.addClientRow(client, this::selectClient);
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load users.");
        }
    }

    private void selectClient(Client client, HBox rowHBox) {
        if (selectedClientRow != null) {
            selectedClientRow.getStyleClass().remove("row-selected");
            selectedClient = null;
            selectedClientRow = null;
            return;
        }

        selectedClient = client;
        selectedClientRow = rowHBox;
        rowHBox.getStyleClass().add("row-selected");
    }

    private void registerRow(VBox tableBox, HBox row, Object data, Runnable onFocus) {
        row.setFocusTraversable(true);
        row.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (focused) onFocus.run();
        });
        row.setUserData(data);
        tableBox.getChildren().add(row);
    }

    private void openClientForm(Client client) {
        try {
            ViewHandler handler = client == null ? ViewHandler.CREATE_CLIENT : ViewHandler.EDIT_CLIENT;
            handler.reset();
            handler.preLoad();

            ClientFormController controller = handler.getController();
            Stage stage = handler.prepareStage();
            controller.setModel(stage, modelFacade, client);

            stage.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                    event.consume();
                }
            });

            stage.showAndWait();

            loadClients(); // refresh the list after the form closes
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the user form. Please try again.");
        }
    }

    @FXML
    private void onClickCreateClient() {openClientForm(null);}

    @FXML private void onClickUpdateClient() {
        if (selectedClient == null) {
            AlertHelper.showError("No Selection", "Please select a client to edit.");
            return;
        }
        openClientForm(selectedClient);
    }

    @FXML private void onClickDeleteClient() {
        if (selectedClient == null) {
            AlertHelper.showError("No Selection", "Please select a client to delete.");
            return;
        }

        AlertHelper.showConfirmation("Delete Client", "Are you sure you want to delete \"" + selectedClient.getClientName() + "\"? This action cannot be undone.", () -> {
            try {
                modelFacade.getClientModel().deleteClient(selectedClient);
                modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), selectedClient.getClientId(), EntityType.CLIENT, LogAction.DELETE, LocalDateTime.now()));
                loadClients();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Delete Failed", "Failed to delete client. Please try again.");
            }
        });
    }

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        return Map.of(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::onClickCreateClient,
                new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), this::onClickUpdateClient,
                new KeyCodeCombination(KeyCode.DELETE), this::onClickDeleteClient
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadClients();
        pgClients.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> loadClients()));

    }
}
