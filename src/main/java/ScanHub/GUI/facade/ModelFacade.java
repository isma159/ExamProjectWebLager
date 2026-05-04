package ScanHub.GUI.facade;

import ScanHub.GUI.models.DocumentMetadataModel;
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    public UserModel userModel = new UserModel();
    public ProfileModel profileModel = new ProfileModel();
    public DocumentMetadataModel metadataModel = new DocumentMetadataModel();

    public ModelFacade() throws Exception {}

    // ── convenience pass-throughs (keeps controllers thin) ──────────────────

    public String getUserFromUsername(String username) throws Exception {
        return userModel.getUserFromUsername(username) != null
                ? userModel.getUserFromUsername(username).getUsername()
                : null;
    }
}