package ScanHub.BLL;

// project imports
import ScanHub.BE.File;
import ScanHub.DAL.facade.DAOFacade;

public class FileManager {

    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public FileManager() {}

    public void moveFile(File file, int newDocumentId) throws Exception {
        daoFacade.getFileDAO().moveFile(file.getFileId(), newDocumentId);
    }
}
