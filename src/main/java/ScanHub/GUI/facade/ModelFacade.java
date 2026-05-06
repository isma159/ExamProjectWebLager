package ScanHub.GUI.facade;

// project imports
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.models.BoxMetadataModel;
import ScanHub.GUI.models.ClientModel;
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;
    private BoxMetadataModel metadataModel;
    private ClientModel clientModel;

    public ModelFacade() throws Exception {
        encrypter = new PasswordEncrypter();
        clientModel = new ClientModel();
        userModel = new UserModel();
        profileModel = new ProfileModel();
        metadataModel = new BoxMetadataModel();
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

    public BoxMetadataModel getMetadataModel() {
        return metadataModel;
    }

    public ClientModel getClientModel() {
        return clientModel;
    }
}
