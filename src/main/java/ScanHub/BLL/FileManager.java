package ScanHub.BLL;

// project imports
import ScanHub.BE.File;
import ScanHub.DAL.DAO.FileDAO;
import ScanHub.DAL.interfaces.IDataAccess;

public class FileManager {

    private final FileDAO fileDAO;

    public FileManager() throws Exception {
        this.fileDAO = new FileDAO();
    }

    public void moveFile(File file, int newDocumentId) throws Exception {
        fileDAO.moveFile(file.getFileId(), newDocumentId);
    }
}
