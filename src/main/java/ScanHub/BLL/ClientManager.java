package ScanHub.BLL;

import ScanHub.BE.Client;
import ScanHub.BLL.facade.DAOFacade;

import java.util.List;

public class ClientManager {

    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public ClientManager() {}

    public Client createClient(Client client) throws Exception {
        return daoFacade.getClientDAO().createData(client);
    }

    public List<Client> getClients() throws Exception {
        return daoFacade.getClientDAO().getData();
    }

    public Client getClientFromName(String name) throws Exception {
        return daoFacade.getClientDAO().getDataFromName(name);
    }

    public void updateClient(Client client) throws Exception {
        daoFacade.getClientDAO().updateData(client);
    }

    public void deleteClient(Client client) throws Exception {
        daoFacade.getClientDAO().deleteData(client);
    }
}
