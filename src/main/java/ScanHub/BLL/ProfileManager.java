package ScanHub.BLL;

// project imports
import ScanHub.BE.Profile;
import ScanHub.DAL.DAO.ProfileDAO;
import ScanHub.DAL.interfaces.IDataAccess;
import ScanHub.GUI.facade.DAOFacade;

// java imports
import java.util.Collections;
import java.util.List;

public class ProfileManager {
    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public ProfileManager() {}

    public Profile createProfile(Profile newProfile) throws Exception {
        return daoFacade.getProfileDAO().createData(newProfile);
    }

    public List<Profile> getProfiles() throws Exception {
        return daoFacade.getProfileDAO().getData();
    }

    public void updateProfile(Profile updatedProfile) throws Exception {
        daoFacade.getProfileDAO().updateData(updatedProfile);
    }

    public void deleteProfile(Profile selectedProfile) throws Exception {
        daoFacade.getProfileDAO().deleteData(selectedProfile);
    }
}
