package ScanHub.GUI.models;

import ScanHub.BE.Box;
import ScanHub.BE.Profile;
import ScanHub.BLL.BoxManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BoxModel {

    private final BoxManager boxManager;
    private final ObservableList<Box> boxObservableList;

    public BoxModel() throws Exception {
        boxManager = new BoxManager();
        boxObservableList = FXCollections.observableArrayList();
        boxObservableList.setAll(boxManager.getBoxes());
    }

    public Box createBox(Box box) throws Exception {
        Box created = boxManager.createBox(box);
        boxObservableList.add(0, created);
        return created;
    }

    public ObservableList<Box> getBoxes() { return boxObservableList; }

    public void refreshBoxes() throws Exception { boxObservableList.setAll(boxManager.getBoxes()); }

    /**
     * Core entry point for starting a scan session.
     * Finds an existing box by ID or name, or creates a new one.
     * Also associates the given profile and loads any persisted documents.
     */
    public Box getOrCreateSessionBox(String boxInput, Profile profile) throws Exception {
        Box box = boxManager.getOrCreateSessionBox(boxInput, profile);

        // keep the observable list in sync if a new box was just created
        boolean alreadyTracked = boxObservableList.stream()
                .anyMatch(b -> b.getBoxId() == box.getBoxId());
        if (!alreadyTracked) {
            boxObservableList.add(0, box); // newest first, matching DAO order
        }

        return box;
    }

    public void updateBox(Box box) throws Exception {
        boxManager.updateBox(box);
        for (int i = 0; i < boxObservableList.size(); i++) {
            if (boxObservableList.get(i).getBoxId() == box.getBoxId()) {
                boxObservableList.set(i, box);
                break;
            }
        }
    }

    public void deleteBox(Box box) throws Exception {
        boxManager.deleteBox(box);
        boxObservableList.removeIf(b -> b.getBoxId() == box.getBoxId());
    }
}