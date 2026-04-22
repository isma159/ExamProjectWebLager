package ScanHub.BLL;

import ScanHub.BE.User;
import ScanHub.DAL.DAO.UserDAO;
import ScanHub.DAL.interfaces.IDataAccess;

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
        return Collections.singletonList(dataAccess.getData());
    }

    public void updateUser(User updatedUser) throws Exception {
        dataAccess.updateData(updatedUser);
    }

    public void deleteUser(User selectedUser) throws Exception {
        dataAccess.deleteData(selectedUser);
    }
}
