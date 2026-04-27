package ScanHub.DAL.interfaces;

import java.util.List;

public interface IDataAccess<T> {

    T createData(T data) throws Exception;
    List<T> getData() throws Exception;
    T getDataFromName(String name) throws Exception;
    void updateData(T newData) throws Exception;
    void deleteData(T data) throws Exception;

}
