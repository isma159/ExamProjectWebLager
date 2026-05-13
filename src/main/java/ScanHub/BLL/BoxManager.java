package ScanHub.BLL;

// project imports
import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.Profile;
import ScanHub.DAL.DAO.BoxDAO;
import ScanHub.DAL.DAO.DocumentDAO;
import ScanHub.DAL.interfaces.IDataAccess;

import java.util.List;

public class BoxManager {
    private IDataAccess<Box> dataAccess;
    private BoxDAO boxDAO;

    public BoxManager() throws Exception {
        boxDAO = new BoxDAO();
        dataAccess = boxDAO;
    }

    public Box createBox(Box box) throws Exception {
        return dataAccess.createData(box);
    }

    public List<Box> getBoxes() throws Exception {
        return dataAccess.getData();
    }

    public Box getBoxFromName(String name) throws Exception {
        return dataAccess.getDataFromName(name);
    }

    public Box getBoxFromId(int boxId) throws Exception {
        return boxDAO.getDataFromId(boxId);
    }

    public void updateBox(Box box) throws Exception {
        dataAccess.updateData(box);
    }

    public void deleteBox(Box box) throws Exception {
        dataAccess.deleteData(box);
    }

    /**
     *
     * @param boxInput
     * @return
     * @throws Exception
     */
    private Box tryGetExistingBox(String boxInput) throws Exception {
        try {
            int boxId = Integer.parseInt(boxInput);
            Box box = getBoxFromId(boxId);
            if (box != null) {
                return box;
            }
        } catch (NumberFormatException ignored) {
        }

        return getBoxFromName(boxInput);
    }

    /**
     * The core of BoxManager
     * TODO: add detailed description
     * @param boxInput
     * @param profile
     * @return
     * @throws Exception
     */
    public Box getOrCreateSessionBox(String boxInput, Profile profile) throws Exception {
        if (profile == null) {
            throw new IllegalArgumentException("A profile is required to start a scan session");
        }

        Box existing = tryGetExistingBox(boxInput);
        if (existing != null) {
            existing.setProfile(profile);
            if (existing.getProfileId() != profile.getProfileId()) {
                existing.setProfileId(profile.getProfileId());
                updateBox(existing);
            }

            // load persisted documents + files into the in-memory box
            DocumentDAO documentDAO = new DocumentDAO();
            List<Document> docs = documentDAO.getDocumentsWithFilesByBoxId(existing.getBoxId());
            existing.getDocuments().addAll(docs);

            return existing;
        }

        Box box = new Box();
        box.setBoxName(boxInput);
        box.setProfileId(profile.getProfileId());
        box.setProfile(profile);
        return createBox(box);
    }
}
