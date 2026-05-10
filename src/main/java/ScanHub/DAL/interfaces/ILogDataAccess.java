package ScanHub.DAL.interfaces;

import ScanHub.BE.Log;

import java.util.List;

public interface ILogDataAccess {

    Log createLog(Log log) throws Exception;

    List<Log> getLogs() throws Exception;


}
