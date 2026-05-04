package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.File;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

// java imports
import java.io.IOException;
import java.util.List;

public class FileDAO implements IDataAccess<File> {

    DBConnector dbConnector = new DBConnector();

    public FileDAO() throws IOException {
    }

    @Override
    public File createData(File file) throws Exception {
        return null;
    }

    @Override
    public List<File> getData() throws Exception {
        return List.of();
    }

    @Override
    public File getDataFromName(String name) throws Exception {
        return null;
    }

    @Override
    public void updateData(File newData) throws Exception {

    }

    @Override
    public void deleteData(File data) throws Exception {

    }
}
