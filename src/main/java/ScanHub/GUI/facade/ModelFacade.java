package ScanHub.GUI.facade;

// project imports
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.DAL.DAO.FileDAO;
import ScanHub.GUI.models.*;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;
    private BoxMetadataModel metadataModel;
    private ClientModel clientModel;
    private FileModel fileModel;

    public ModelFacade() throws Exception {
        encrypter = new PasswordEncrypter();
        clientModel = new ClientModel();
        userModel = new UserModel();
        profileModel = new ProfileModel();
        metadataModel = new BoxMetadataModel();
        fileModel = new FileModel();
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

    public FileModel getFileModel() {
        return fileModel;
    }
}
