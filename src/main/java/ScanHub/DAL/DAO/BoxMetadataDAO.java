package ScanHub.DAL.DAO;

import ScanHub.BE.BoxMetadata;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IMetadataDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoxMetadataDAO implements IMetadataDataAccess {

    DBConnector dbConnector = new DBConnector();

    public BoxMetadataDAO() throws IOException {}

    @Override
    public BoxMetadata createData(BoxMetadata metadata) throws Exception {
        return metadata != null ? getDataByBoxId(metadata.getBoxId()) : null;
    }

    @Override
    public List<BoxMetadata> getData() throws Exception {
        List<BoxMetadata> metadataList = new ArrayList<>();

        String sql = """
                SELECT boxId, boxName, profileName, documentCount, fileCount, created_at
                FROM vw_BoxMetadata
                ORDER BY created_at DESC
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                metadataList.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new Exception("Could not retrieve box metadata list", e);
        }

        return metadataList;
    }

    @Override
    public BoxMetadata getDataFromId(int metadataId) throws Exception {
        return getDataByBoxId(metadataId);
    }

    @Override
    public BoxMetadata getDataByBoxId(int boxId) throws Exception {
        String sql = """
                SELECT boxId, boxName, profileName, documentCount, fileCount, created_at
                FROM vw_BoxMetadata
                WHERE boxId = ?
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, boxId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new Exception("Could not fetch metadata for boxId " + boxId, e);
        }
    }

    @Override
    public void updateData(BoxMetadata metadata) {}

    @Override
    public void deleteData(BoxMetadata metadata) {}

    private BoxMetadata mapRow(ResultSet rs) throws SQLException {
        Timestamp boxCreatedAt = rs.getTimestamp("created_at");

        BoxMetadata meta = new BoxMetadata(
                rs.getInt("boxId"),
                rs.getInt("boxId"),
                rs.getString("profileName"),
                rs.getInt("documentCount"),
                rs.getInt("fileCount"),
                boxCreatedAt != null ? boxCreatedAt.toLocalDateTime() : null
        );

        meta.setBoxName(rs.getString("boxName"));
        return meta;
    }
}
