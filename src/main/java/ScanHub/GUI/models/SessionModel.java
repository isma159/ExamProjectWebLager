package ScanHub.GUI.models;

import ScanHub.BE.User;
import ScanHub.BLL.SessionManager;

public class SessionModel {

    private final SessionManager sessionManager;

    public SessionModel() { sessionManager = new SessionManager(); }
    public User getCurrentUser() { return sessionManager.getCurrentUser(); }
    public boolean login(User user) { return sessionManager.login(user); }
    public void logout() { sessionManager.logout(); }
}