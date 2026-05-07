package ScanHub.GUI.facade;

// project imports
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.models.*;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;
    private BoxModel boxModel;
    private BoxMetadataModel metadataModel;
    private ClientModel clientModel;

    public ModelFacade() throws Exception {
        encrypter = new PasswordEncrypter();
        clientModel = new ClientModel();
        userModel = new UserModel();
        profileModel = new ProfileModel();
        metadataModel = new BoxMetadataModel();
        boxModel = new BoxModel();
    }

    public PasswordEncrypter getEncrypter() { return encrypter; }

    public UserModel getUserModel() { return userModel; }

    public ProfileModel getProfileModel() { return profileModel; }

    public BoxModel getBoxModel() { return boxModel; }

    public BoxMetadataModel getMetadataModel() { return metadataModel; }

    public ClientModel getClientModel() { return clientModel; }
}
