package ScanHub.GUI.models;

import ScanHub.BE.BoxMetadata;
import ScanHub.BLL.BoxMetadataManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BoxMetadataModel {

    private ObservableList<BoxMetadata> metadataObservableList;
    private final BoxMetadataManager manager;

    public BoxMetadataModel() throws Exception {
        manager = new BoxMetadataManager();
        metadataObservableList = FXCollections.observableArrayList();
        metadataObservableList.setAll(manager.getAllMetadata());
    }

    public void createMetadata(BoxMetadata metadata) throws Exception {
        BoxMetadata created = manager.createMetadata(metadata);
        metadataObservableList.add(created);
    }

    public ObservableList<BoxMetadata> getAllMetadata() { return metadataObservableList; }

    public BoxMetadata getMetadataByBoxId(int boxId) throws Exception {
        return manager.getMetadataByBoxId(boxId);
    }

    public void refreshMetadataModel() throws Exception {
        metadataObservableList.setAll(manager.getAllMetadata());
    }

    public void updateMetadata(BoxMetadata metadata) throws Exception {
        manager.updateMetadata(metadata);
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

    public void deleteMetadata(BoxMetadata metadata) throws Exception {
        manager.deleteMetadata(metadata);
        metadataObservableList.remove(metadata);
    }
}
