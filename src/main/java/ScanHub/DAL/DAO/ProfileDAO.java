package ScanHub.DAL.DAO;

import ScanHub.BE.Profile;
import ScanHub.DAL.interfaces.IDataAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProfileDAO implements IDataAccess<Profile> {

    @Override
    public Profile createData(Profile data) throws Exception {
        return null;
    }

    @Override
    public List<Profile> getData() throws Exception {
        return null;
    }

    @Override
    public void updateData(Profile newData) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteData(Profile data) throws Exception {
        throw new UnsupportedOperationException();
    }
}
