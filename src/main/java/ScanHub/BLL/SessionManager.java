package ScanHub.BLL;

import ScanHub.BE.User;
import ScanHub.BLL.facade.DAOFacade;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    private User currentUser;
    private ScheduledExecutorService heartbeat;
    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public SessionManager() {}

    public boolean login(User user) {
        if (currentUser == null) {
            currentUser = user;
            return true;
        }
        else {
            return false;
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // tries to create a session in db and starts refreshing. Locked will return false if the box already has an active session.
    public boolean tryStartScanSession(String boxName) throws SQLException {
        boolean locked = daoFacade.getSessionDAO().tryInsertSession(currentUser, boxName);
        if (locked) {startHeartbeat(boxName);}
        return locked;
    }

    // stops refreshing the session, and deletes session from db.
    public void endScanSession(String boxName) throws SQLException {
        stopHeartbeat();
        daoFacade.getSessionDAO().deleteSession(boxName);
    }

    // starts refreshing the session every 10 minutes
    private void startHeartbeat(String boxName) {
        if (heartbeat == null || heartbeat.isShutdown()) {
            heartbeat = Executors.newSingleThreadScheduledExecutor();
            heartbeat.scheduleAtFixedRate(() -> {
                try {
                    daoFacade.getSessionDAO().refreshSession(boxName);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }, 10, 10, TimeUnit.MINUTES);
        }
    }

    // stops constant method invocation cycle
    private void stopHeartbeat() {
        if (heartbeat != null || !heartbeat.isShutdown()) {
            heartbeat.shutdown();
            heartbeat = null;
        }
    }

    public void cleanupStaleSessions() throws SQLException {
        daoFacade.getSessionDAO().cleanupStaleSessions();
    }

}
