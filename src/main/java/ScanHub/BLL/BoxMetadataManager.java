package ScanHub.BLL;

import ScanHub.BE.BoxMetadata;
import ScanHub.DAL.DAO.BoxMetadataDAO;
import ScanHub.DAL.interfaces.IMetadataDataAccess;
import ScanHub.GUI.facade.DAOFacade;

import java.util.List;

public class BoxMetadataManager {

    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public BoxMetadataManager() {}

    public BoxMetadata createMetadata(BoxMetadata metadata) throws Exception {
        return daoFacade.getBoxMetadataDAO().createData(metadata);
    }

    public List<BoxMetadata> getAllMetadata() throws Exception {
        return daoFacade.getBoxMetadataDAO().getData();
    }

    public BoxMetadata getMetadataById(int metadataId) throws Exception {
        return daoFacade.getBoxMetadataDAO().getDataFromId(metadataId);
    }

    public BoxMetadata getMetadataByBoxId(int boxId) throws Exception {
        return daoFacade.getBoxMetadataDAO().getDataByBoxId(boxId);
    }

    public void updateMetadata(BoxMetadata metadata) throws Exception {
        daoFacade.getBoxMetadataDAO().updateData(metadata);
    }

    public void deleteMetadata(BoxMetadata metadata) throws Exception {
        daoFacade.getBoxMetadataDAO().deleteData(metadata);
    }
}
