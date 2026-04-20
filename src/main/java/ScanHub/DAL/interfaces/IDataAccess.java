package ScanHub.DAL.interfaces;

public interface IDataAccess<T> {

    T getData() throws Exception;
    T addData(T data) throws Exception;
    T updateData(T newData) throws Exception;
    T deleteData(T data) throws Exception;

}
