package ScanHub.GUI.facade;

import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    private PasswordEncrypter encrypter;
    private UserModel userModel;
    private ProfileModel profileModel;

     public ModelFacade() throws Exception {
         userModel = new UserModel();
         profileModel = new ProfileModel();
         encrypter = new PasswordEncrypter();
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
