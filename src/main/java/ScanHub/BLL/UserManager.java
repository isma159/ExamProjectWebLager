package ScanHub.BLL;

// project imports
import ScanHub.BE.User;
import ScanHub.DAL.DAO.UserDAO;
import ScanHub.DAL.interfaces.IDataAccess;

//java imports
import java.util.Collections;
import java.util.List;

public class UserManager {
    private IDataAccess<User> dataAccess;

    public UserManager() throws Exception {
        dataAccess = new UserDAO();
    }

    public User createUser(User newUser) throws Exception {
        return dataAccess.createData(newUser);
    }

    public List<User> getUsers() throws Exception {
        return dataAccess.getData();
    }

    public User getUserFromUsername(String username) throws Exception {
        return dataAccess.getDataFromName(username);
    }

    public void updateUser(User updatedUser) throws Exception {
        dataAccess.updateData(updatedUser);
    }

    public void deleteUser(User selectedUser) throws Exception {
        dataAccess.deleteData(selectedUser);
    }
}
