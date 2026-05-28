package ScanHub.BLL;

// project imports
import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.Profile;
import ScanHub.DAL.facade.DAOFacade;

import java.util.List;

public class BoxManager {
    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public BoxManager() {}

    public Box createBox(Box box) throws Exception {
        return daoFacade.getBoxDAO().createData(box);
    }

    public List<Box> getBoxes() throws Exception {
        return daoFacade.getBoxDAO().getData();
    }

    public Box getBoxFromName(String name) throws Exception {
        return daoFacade.getBoxDAO().getDataFromName(name);
    }

    public Box getBoxFromId(int boxId) throws Exception {
        return daoFacade.getBoxDAO().getDataFromId(boxId);
    }

    public void updateBox(Box box) throws Exception {
        daoFacade.getBoxDAO().updateData(box);
    }

    public void deleteBox(Box box) throws Exception {
        daoFacade.getBoxDAO().deleteData(box);
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

    public Box getOrCreateSessionBox(String boxInput, Profile profile) throws Exception {
        if (profile == null) {
            throw new IllegalArgumentException("A profile is required to start a scan session");
        }

        Box existing = tryGetExistingBox(profile.getExportLabel() + boxInput);
        if (existing != null) {
            existing.setProfile(profile);
            if (existing.getProfileId() != profile.getProfileId()) {
                existing.setProfileId(profile.getProfileId());
                updateBox(existing);
            }

            // load persisted documents + files into the in-memory box
            List<Document> docs = daoFacade.getDocumentDAO().getDocumentsWithFilesByBoxId(existing.getBoxId());
            existing.getDocuments().clear();
            existing.getDocuments().addAll(docs);

            return existing;
        }

        Box box = new Box();
        box.setBoxName(profile.getExportLabel() + boxInput);
        box.setProfileId(profile.getProfileId());
        box.setProfile(profile);
        box.setStaged(true);

        return box;
    }
}
