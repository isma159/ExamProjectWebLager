package ScanHub.BLL;

import ScanHub.BE.BoxMetadata;
import ScanHub.DAL.DAO.BoxMetadataDAO;
import ScanHub.DAL.interfaces.IMetadataDataAccess;

import java.util.List;

public class BoxMetadataManager {

    private final IMetadataDataAccess dataAccess;

    public BoxMetadataManager() throws Exception {
        dataAccess = new BoxMetadataDAO();
    }

    public BoxMetadata createMetadata(BoxMetadata metadata) throws Exception {
        return dataAccess.createData(metadata);
    }

    public List<BoxMetadata> getAllMetadata() throws Exception {
        return dataAccess.getData();
    }

    public BoxMetadata getMetadataById(int metadataId) throws Exception {
        return dataAccess.getDataFromId(metadataId);
    }

    public BoxMetadata getMetadataByBoxId(int boxId) throws Exception {
        return dataAccess.getDataByBoxId(boxId);
    }

    public void updateMetadata(BoxMetadata metadata) throws Exception {
        dataAccess.updateData(metadata);
    }

    public void deleteMetadata(BoxMetadata metadata) throws Exception {
        dataAccess.deleteData(metadata);
    }
}
