package ScanHub.BLL;

import ScanHub.BE.Profile;
import ScanHub.DAL.DAO.ProfileDAO;
import ScanHub.DAL.interfaces.IDataAccess;

import java.util.Collections;
import java.util.List;

public class ProfileManager {
    private IDataAccess<Profile> dataAccess;

    public ProfileManager() throws Exception {
        dataAccess = new ProfileDAO();
    }

    public Profile createProfile(Profile newProfile) throws Exception {
        return dataAccess.createData(newProfile);
    }

    public List<Profile> getProfiles() throws Exception {
        return dataAccess.getData();
    }

    public void updateProfile(Profile updatedProfile) throws Exception {
        dataAccess.updateData(updatedProfile);
    }

    public void deleteProfile(Profile selectedProfile) throws Exception {
        dataAccess.deleteData(selectedProfile);
    }
}
