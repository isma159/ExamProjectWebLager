package ScanHub.GUI.facade;

import ScanHub.BLL.DocumentMetadataManager;
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;
    private DocumentMetadataManager metadataManager;

    public ModelFacade() throws Exception {
        encrypter = new PasswordEncrypter();
        userModel = new UserModel();
        profileModel = new ProfileModel();
        metadataManager = new DocumentMetadataManager();

    }

    public PasswordEncrypter getEncrypter() {
        return encrypter;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public ProfileModel getProfileModel() {
        return profileModel;
    }

    public DocumentMetadataManager getMetadataManager() {
        return metadataManager;
    }
}
