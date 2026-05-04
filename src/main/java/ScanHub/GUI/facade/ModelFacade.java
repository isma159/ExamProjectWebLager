package ScanHub.GUI.facade;

import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;

    public PasswordEncrypter getEncrypter() {
        if (encrypter == null) {
            encrypter = new PasswordEncrypter();
        }
        return encrypter;
    }

    public UserModel getUserModel() throws Exception {
        if (userModel == null) {
            userModel = new UserModel();
        }
        return userModel;
    }

    public ProfileModel getProfileModel() throws Exception {
        if (profileModel == null) {
            profileModel = new ProfileModel();
        }
        return profileModel;
    }
}
