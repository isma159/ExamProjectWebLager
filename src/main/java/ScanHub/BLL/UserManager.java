package ScanHub.BLL;

// project imports
import ScanHub.BE.User;
import ScanHub.DAL.facade.DAOFacade;

//java imports
import java.util.List;

public class UserManager {
    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public UserManager() {}

    public User createUser(User newUser) throws Exception {
        return daoFacade.getUserDAO().createData(newUser);
    }

    public List<User> getUsers() throws Exception {
        return daoFacade.getUserDAO().getData();
    }

    public User getUserFromUsername(String username) throws Exception {
        return daoFacade.getUserDAO().getDataFromName(username);
    }

    public void updateUser(User updatedUser) throws Exception {
        daoFacade.getUserDAO().updateData(updatedUser);
    }

    public void deleteUser(User selectedUser) throws Exception {
        daoFacade.getUserDAO().deleteData(selectedUser);
    }
}
