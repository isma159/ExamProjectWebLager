package ScanHub.BLL;

import ScanHub.BE.File;
import ScanHub.DAL.interfaces.IDataAccess;

public class FileManager {
    private IDataAccess<File> dataAccess;

    public FileManager() throws Exception {
        //dataAccess = new FileDAO();
    }
}
