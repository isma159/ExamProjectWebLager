package ScanHub.BLL;

import ScanHub.BE.Client;
import ScanHub.DAL.DAO.ClientDAO;
import ScanHub.DAL.interfaces.IDataAccess;

import java.util.List;

public class ClientManager {

    private IDataAccess<Client> dataAccess;

    public ClientManager() throws Exception {
        dataAccess = new ClientDAO();
    }

    public Client createClient(Client client) throws Exception {
        return dataAccess.createData(client);
    }

    public List<Client> getClients() throws Exception {
        return dataAccess.getData();
    }

    public Client getClientFromName(String name) throws Exception {
        return dataAccess.getDataFromName(name);
    }

    public void updateClient(Client client) throws Exception {
        dataAccess.updateData(client);
    }

    public void deleteClient(Client client) throws Exception {
        dataAccess.deleteData(client);
    }
}
