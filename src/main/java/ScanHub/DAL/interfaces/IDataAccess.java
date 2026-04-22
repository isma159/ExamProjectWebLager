package ScanHub.DAL.interfaces;

public interface IDataAccess<T> {

    T createData(T data) throws Exception;
    T getData() throws Exception;
    T updateData(T newData) throws Exception;
    T deleteData(T data) throws Exception;

}
