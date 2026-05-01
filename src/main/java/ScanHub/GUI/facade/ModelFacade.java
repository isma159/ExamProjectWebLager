package ScanHub.GUI.facade;

import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    private UserModel userModel;
    private ProfileModel profileModel;

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
