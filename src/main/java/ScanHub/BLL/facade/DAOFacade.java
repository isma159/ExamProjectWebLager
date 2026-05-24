package ScanHub.BLL.facade;

import ScanHub.BE.*;
import ScanHub.DAL.DAO.*;
import ScanHub.DAL.interfaces.IDataAccess;

public class DAOFacade {

    private static DAOFacade daoFacade = new DAOFacade();

    private final IDataAccess<User> userDAO;
    private final IDataAccess<Profile> profileDAO;
    private final IDataAccess<Client> clientDAO;
    private final IDataAccess<Log> logDAO;
    private final IDataAccess<Box> boxDAO;
    private final DocumentDAO documentDAO;
    private final FileDAO fileDAO;
    private final BoxMetadataDAO boxMetadataDAO;
    private final SessionDAO sessionDAO;

    private DAOFacade() {
        userDAO = new UserDAO();
        profileDAO = new ProfileDAO();
        clientDAO = new ClientDAO();
        logDAO = new LogDAO();
        boxDAO = new BoxDAO();
        documentDAO = new DocumentDAO();
        fileDAO = new FileDAO();
        boxMetadataDAO = new BoxMetadataDAO();
        sessionDAO = new SessionDAO();
    }

    public static DAOFacade getInstance() {return daoFacade;}

    public IDataAccess<User> getUserDAO() {return userDAO;}
    public IDataAccess<Profile> getProfileDAO() {return profileDAO;}
    public IDataAccess<Client> getClientDAO() {return clientDAO;}
    public IDataAccess<Log> getLogDAO() {return logDAO;}
    public IDataAccess<Box> getBoxDAO() {return boxDAO;}
    public DocumentDAO getDocumentDAO() {return documentDAO;}
    public FileDAO getFileDAO() {return fileDAO;}
    public BoxMetadataDAO getBoxMetadataDAO() {return boxMetadataDAO;}
    public SessionDAO getSessionDAO() {return sessionDAO;}

}
