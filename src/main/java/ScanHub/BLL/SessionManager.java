package ScanHub.BLL;

import ScanHub.BE.User;
import ScanHub.GUI.util.AlertHelper;

public class SessionManager {

    private User currentUser;

    public SessionManager() {}

    public boolean login(User user) {
        if (currentUser == null) {
            currentUser = user;
            return true;
        }
        else {
            AlertHelper.showError("Session Already Active", "A user is already logged in. Please log out before starting a new session.");
            return false;
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

}
