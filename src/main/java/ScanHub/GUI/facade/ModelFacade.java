package ScanHub.GUI.facade;

// project imports
import ScanHub.BLL.DocumentMetadataManager;
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.models.DocumentMetadataModel;
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;
    private DocumentMetadataManager metadataManager;
    private DocumentMetadataModel metadataModel;

    public ModelFacade() throws Exception {
        encrypter = new PasswordEncrypter();
        userModel = new UserModel();
        profileModel = new ProfileModel();
        metadataManager = new DocumentMetadataManager();
        metadataModel = new DocumentMetadataModel();

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
    public DocumentMetadataModel getMetadataModel() {
        return metadataModel;
    }
}
