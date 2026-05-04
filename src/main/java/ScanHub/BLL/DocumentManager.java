package ScanHub.BLL;

// project imports
import ScanHub.BE.Document;
import ScanHub.DAL.interfaces.IDataAccess;

public class DocumentManager {
    private IDataAccess<Document> dataAccess;

    public DocumentManager() throws Exception {
        //dataAccess = new DocumentDAO();
    }
}
