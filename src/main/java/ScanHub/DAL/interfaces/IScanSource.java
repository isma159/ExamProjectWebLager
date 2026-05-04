package ScanHub.DAL.interfaces;

import ScanHub.DAL.ApiClient.ScanResult;

public interface IScanSource {
    ScanResult fetchNextScan() throws Exception;
}