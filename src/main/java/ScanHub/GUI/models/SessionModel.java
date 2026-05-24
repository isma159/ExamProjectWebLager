package ScanHub.GUI.models;

import ScanHub.BE.User;
import ScanHub.BLL.SessionManager;

import java.sql.SQLException;

public class SessionModel {

    private final SessionManager sessionManager;

    public SessionModel() { sessionManager = new SessionManager(); }
    public User getCurrentUser() { return sessionManager.getCurrentUser(); }
    public boolean login(User user) { return sessionManager.login(user); }
    public void logout() { sessionManager.logout(); }

    public boolean tryStartScanSession(String boxName) throws SQLException {
        return sessionManager.tryStartScanSession(boxName);
    }
    public void endScanSession(String boxName) throws SQLException {
        sessionManager.endScanSession(boxName);
    }

    public void cleanup() throws SQLException {

        sessionManager.cleanupStaleSessions();

    }
}