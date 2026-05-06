package ScanHub.GUI.facade;

// project imports
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;

    public ModelFacade() throws Exception {
        PasswordEncrypter encrypter = new PasswordEncrypter();
        UserModel userModel = new UserModel();
        ProfileModel profileModel = new ProfileModel();
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
}
