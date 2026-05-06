package ScanHub.GUI.models;

import ScanHub.BE.DocumentMetadata;
import ScanHub.BLL.DocumentMetadataManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DocumentMetadataModel {

    private ObservableList<DocumentMetadata> metadataObservableList;
    private final DocumentMetadataManager manager;

    public DocumentMetadataModel() throws Exception {
        manager = new DocumentMetadataManager();
        metadataObservableList = FXCollections.observableArrayList();
        metadataObservableList.setAll(manager.getAllMetadata());
    }

    public void createMetadata(DocumentMetadata metadata) throws Exception {
        DocumentMetadata created = manager.createMetadata(metadata);
        metadataObservableList.add(created);
    }

    public ObservableList<DocumentMetadata> getAllMetadata() {
        return metadataObservableList;
    }

    public DocumentMetadata getMetadataByDocumentId(int documentId) throws Exception {
        return manager.getMetadataByDocumentId(documentId);
    }

    public void refreshModel() throws Exception {
        metadataObservableList.setAll(manager.getAllMetadata());
    }

    public void updateMetadata(DocumentMetadata metadata) throws Exception {
        manager.updateMetadata(metadata);
        // Refresh the matching entry in the observable list so the table updates automatically
        int index = -1;
        for (int i = 0; i < metadataObservableList.size(); i++) {
            if (metadataObservableList.get(i).getMetadataId() == metadata.getMetadataId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            metadataObservableList.set(index, metadata);
        }
    }

    public void deleteMetadata(DocumentMetadata metadata) throws Exception {
        manager.deleteMetadata(metadata);
        metadataObservableList.remove(metadata);
    }
}