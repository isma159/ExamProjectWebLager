package ScanHub.BLL;

import ScanHub.BE.Profile;
import ScanHub.DAL.interfaces.IDataAccess;

public class ProfileManager {
    private IDataAccess<Profile> dataAccess;

    public ProfileManager() throws Exception {
        //dataAccess = new ProfileDAO();
    }
}
