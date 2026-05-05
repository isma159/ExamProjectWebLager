package ScanHub.DAL.interfaces;

import ScanHub.BE.DocumentMetadata;
import java.util.List;

public interface IMetadataDataAccess {

    DocumentMetadata createData(DocumentMetadata metadata) throws Exception;
    List<DocumentMetadata> getData() throws Exception;
    DocumentMetadata getDataFromId(int metadataId) throws Exception;
    DocumentMetadata getDataByDocumentId(int documentId) throws Exception;
    void updateData(DocumentMetadata metadata) throws Exception;
    void deleteData(DocumentMetadata metadata) throws Exception;
}