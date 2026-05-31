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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class AdminClientsController implements Initializable, IShortcutHandler {

    @FXML private Pagination pgClients;
    @FXML private VBox clientTableBox;
    @FXML private TextField txtFldClientSearch;

    private final ModelFacade modelFacade;
    private final Stage currentStage;
    private Client selectedClient;
    private HBox selectedClientRow;

    private final List<HBox> currentRows = new ArrayList<>();
    private int selectedRowIndex = -1;

    private final int TOTAL_TABLE_SIZE = 15;

    public AdminClientsController(ModelFacade modelFacade, Stage currentStage) {

        this.modelFacade = modelFacade;
        this.currentStage = currentStage;

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterClients();
        txtFldClientSearch.textProperty().addListener((observable, oldValue, newValue) -> filterClients());
        pgClients.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> filterClients()));
    }

    private void filterClients() {
        String search = txtFldClientSearch.getText().toLowerCase();

        List<Client> clients = modelFacade.getClientModel().getClients();

        clients = clients.stream().filter(c ->
                search.isBlank() || c.getClientName().toLowerCase().contains(search)
        ).toList();

        loadClients(clients);
    }

    private void loadClients(List<Client> clients) {
        try {
            selectedClient = null;
            selectedClientRow = null;
            selectedRowIndex = -1;
            currentRows.clear();

            TableLoader.loadTable(clientTableBox, pgClients, TOTAL_TABLE_SIZE, clients, item -> {
                Client client = (Client) item;
                return RowMaker.addClientRow(client, this::selectClient, this::openClientForm, this::deleteClient);
            });

            currentRows.addAll(clientTableBox.getChildren().stream()
                    .filter(n -> n instanceof HBox)
                    .map(n -> (HBox) n)
                    .toList());
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load clients.");
        }
    }

    private void selectClient(Client client, HBox rowHBox) {
        if (selectedClientRow != null) {
            selectedClientRow.getStyleClass().remove("row-selected");
        }

        if (selectedClientRow == rowHBox) {
            selectedClient = null;
            selectedClientRow = null;
            selectedRowIndex = -1;
            return;
        }

        selectedClient = client;
        selectedClientRow = rowHBox;
        selectedRowIndex = currentRows.indexOf(rowHBox);
        rowHBox.getStyleClass().add("row-selected");
    }

    /** Moves the selection up or down by one row. */
    private void moveSelection(int delta) {
        if (currentRows.isEmpty()) return;

        int newIndex;
        if (selectedRowIndex < 0) {
            newIndex = delta > 0 ? 0 : currentRows.size() - 1;
        } else {
            newIndex = selectedRowIndex + delta;
            if (newIndex < 0 || newIndex >= currentRows.size()) return;
        }

        HBox targetRow = currentRows.get(newIndex);
        Object userData = targetRow.getUserData();
        if (userData instanceof Client client) {
            selectClient(client, targetRow);
        }
    }

    @FXML
    private void onClickCreateClient() { openClientForm(null); }

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

            filterClients(); // refresh the list after the form closes
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the client form. Please try again.");
        }
    }

    private void deleteClient(Client client) {
        AlertHelper.showConfirmation("Delete Client", "Are you sure you want to delete \"" + client.getClientName() + "\"? This action cannot be undone.", () -> {
            try {
                modelFacade.getClientModel().deleteClient(client);
                modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), client.getClientId(), EntityType.CLIENT, LogAction.DELETE, LocalDateTime.now()));
                filterClients();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Delete Failed", "Failed to delete client. Please try again.");
            }
        });
    }

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        Map<KeyCodeCombination, Runnable> shortcuts = new HashMap<>();
        shortcuts.put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::onClickCreateClient);
        shortcuts.put(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), () -> {
            if (selectedClient == null) { AlertHelper.showWarning("No Selection", "Please select a client to edit."); return; }
            openClientForm(selectedClient);
        });
        shortcuts.put(new KeyCodeCombination(KeyCode.DELETE), () -> {
            if (selectedClient == null) { AlertHelper.showWarning("No Selection", "Please select a client to delete."); return; }
            deleteClient(selectedClient);
        });
        shortcuts.put(new KeyCodeCombination(KeyCode.UP), () -> {
            if (!txtFldClientSearch.isFocused()) moveSelection(-1);
        });
        shortcuts.put(new KeyCodeCombination(KeyCode.DOWN), () -> {
            if (!txtFldClientSearch.isFocused()) moveSelection(1);
        });
        shortcuts.put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> {
            txtFldClientSearch.requestFocus();
            txtFldClientSearch.selectAll();
        });
        return shortcuts;
    }
}
