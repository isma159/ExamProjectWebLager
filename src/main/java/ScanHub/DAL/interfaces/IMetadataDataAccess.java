package ScanHub.DAL.interfaces;

import ScanHub.BE.BoxMetadata;
import java.util.List;

public interface IMetadataDataAccess {

    BoxMetadata createData(BoxMetadata metadata) throws Exception;
    List<BoxMetadata> getData() throws Exception;
    BoxMetadata getDataFromId(int metadataId) throws Exception;
    BoxMetadata getDataByBoxId(int boxId) throws Exception;
    void updateData(BoxMetadata metadata) throws Exception;
    void deleteData(BoxMetadata metadata) throws Exception;
}
