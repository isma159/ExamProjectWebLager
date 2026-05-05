package ScanHub.BLL;

import ScanHub.BE.DocumentMetadata;
import ScanHub.DAL.DAO.DocumentMetadataDAO;
import ScanHub.DAL.interfaces.IMetadataDataAccess;

import java.util.List;

public class DocumentMetadataManager {

    private final IMetadataDataAccess dataAccess;

    public DocumentMetadataManager() throws Exception {
        dataAccess = new DocumentMetadataDAO();
    }

    public DocumentMetadata createMetadata(DocumentMetadata metadata) throws Exception {
        return dataAccess.createData(metadata);
    }

    public List<DocumentMetadata> getAllMetadata() throws Exception {
        return dataAccess.getData();
    }

    public DocumentMetadata getMetadataById(int metadataId) throws Exception {
        return dataAccess.getDataFromId(metadataId);
    }

    public DocumentMetadata getMetadataByDocumentId(int documentId) throws Exception {
        return dataAccess.getDataByDocumentId(documentId);
    }

    public void updateMetadata(DocumentMetadata metadata) throws Exception {
        dataAccess.updateData(metadata);
    }

    public void deleteMetadata(DocumentMetadata metadata) throws Exception {
        dataAccess.deleteData(metadata);
    }
}