package ScanHub.GUI.facade;

// project imports
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.models.*;

public class ModelFacade {

    private final PasswordEncrypter encrypter;
    private final UserModel userModel;
    private final ProfileModel profileModel;
    private final ClientModel clientModel;
    private final BoxModel boxModel;
    private final FileModel fileModel;
    private final BoxMetadataModel metadataModel;
    private final LogModel logModel;


    public ModelFacade() throws Exception {
        encrypter = new PasswordEncrypter();
        userModel = new UserModel();
        profileModel = new ProfileModel();
        clientModel = new ClientModel();
        boxModel = new BoxModel();
        fileModel = new FileModel();
        metadataModel = new BoxMetadataModel();
        logModel = new LogModel();
    }

    public PasswordEncrypter getEncrypter() { return encrypter; }
    public UserModel getUserModel() { return userModel; }
    public ProfileModel getProfileModel() { return profileModel; }
    public ClientModel getClientModel() { return clientModel; }
    public BoxModel getBoxModel() { return boxModel; }
    public FileModel getFileModel() { return fileModel; }
    public BoxMetadataModel getMetadataModel() { return metadataModel; }
    public LogModel getLogModel() { return logModel; }
}
