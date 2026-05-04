package ScanHub.GUI.facade;

// project imports
import ScanHub.GUI.models.ProfileModel;
import ScanHub.GUI.models.UserModel;

public class ModelFacade {
    public UserModel userModel = new UserModel();
    public ProfileModel profileModel = new ProfileModel();

    public ModelFacade() throws Exception {}
}
