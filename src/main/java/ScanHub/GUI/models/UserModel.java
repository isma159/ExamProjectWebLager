package ScanHub.GUI.models;

// project imports
import ScanHub.BE.User;
import ScanHub.BLL.UserManager;

// java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class UserModel {
    private ObservableList<User> userObservableList;
    private UserManager userManager = new UserManager();

    public UserModel() throws Exception {
        userObservableList = FXCollections.observableArrayList();
        userObservableList.setAll(userManager.getUsers());
    }

    public void createUser(User newUser) throws Exception {
        User createdUser = userManager.createUser(newUser);
        userObservableList.add(createdUser);
    }

    public ObservableList<User> getUsers() {
        return userObservableList;
    }

    public void refreshModel() throws Exception {
        userObservableList.setAll(userManager.getUsers());
    }

    public User getUserFromUsername(String username) throws Exception {
        return userManager.getUserFromUsername(username);
    }

    public void updateUser(User updatedUser) throws Exception {
        userManager.updateUser(updatedUser);
        // Update the local observable list so the table refreshes automatically
        int index = userObservableList.indexOf(updatedUser);
        if (index >= 0) {
            userObservableList.set(index, updatedUser);
        }
    }

    public void deleteUser(User selectedUser) throws Exception {
        userManager.deleteUser(selectedUser);
        userObservableList.remove(selectedUser);
    }
}
