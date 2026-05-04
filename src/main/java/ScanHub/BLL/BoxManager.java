package ScanHub.BLL;

// project imports
import ScanHub.BE.Box;
import ScanHub.DAL.interfaces.IDataAccess;

public class BoxManager {
    private IDataAccess<Box> dataAccess;

    public BoxManager() throws Exception {
        //dataAccess = new BoxDAO();
    }
}
