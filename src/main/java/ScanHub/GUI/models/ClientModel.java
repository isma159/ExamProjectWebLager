package ScanHub.GUI.models;

import ScanHub.BE.Client;
import ScanHub.BLL.ClientManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientModel {

    private final ObservableList<Client> clientObservableList;
    private final ClientManager clientManager = new ClientManager();

    public ClientModel() throws Exception {
        clientObservableList = FXCollections.observableArrayList();
        clientObservableList.setAll(clientManager.getClients());
    }

    public Client createClient(Client client) throws Exception {
        Client createdClient = clientManager.createClient(client);
        clientObservableList.add(createdClient);
        return createdClient;
    }

    public ObservableList<Client> getClients() { return clientObservableList; }

    public void refreshClients() throws Exception { clientObservableList.setAll(clientManager.getClients()); }

    public void updateClient(Client client) throws Exception {
        clientManager.updateClient(client);
        int index = clientObservableList.indexOf(client);
        if (index >= 0) {
            clientObservableList.set(index, client);
        }
    }

    public void deleteClient(Client client) throws Exception {
        clientManager.deleteClient(client);
        clientObservableList.remove(client);
    }
}
