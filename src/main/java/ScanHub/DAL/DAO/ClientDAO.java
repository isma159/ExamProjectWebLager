package ScanHub.DAL.DAO;

import ScanHub.BE.Client;
import ScanHub.BE.FileAdjustmentSettings;
import ScanHub.BE.Profile;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO implements IDataAccess<Client> {

    public ClientDAO() {}

    @Override
    public Client createData(Client client) throws Exception {
        String sql = """
                INSERT INTO Clients (clientName)
                OUTPUT INSERTED.clientId
                VALUES (?)
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, client.getClientName());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    client.setClientId(rs.getInt("clientId"));
                    return client;
                }
            }

            throw new SQLException("Insert returned no clientId");
        } catch (SQLException e) {
            throw new Exception("Could not create client", e);
        }
    }

    @Override
    public List<Client> getData() throws Exception {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT clientId, clientName FROM Clients WHERE deleted_at IS NULL ORDER BY clientName";
        String selectProfilesSQL = """
                                       SELECT p.profileId, p.profileName,
                                              p.exportLabel, p.status, p.rotation,
                                              p.hue, p.brightness, p.contrast,
                                              p.saturation
                                       FROM Profiles p
                                       WHERE p.clientId = ? AND p.deleted_at IS NULL
                                       """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             PreparedStatement ps2 = connection.prepareStatement(selectProfilesSQL);
             ResultSet rs = ps.executeQuery()) {

            // loops through each client
            while (rs.next()) {

                Client client = mapRow(rs);
                ps2.setInt(1, client.getClientId());
                try (ResultSet rs2 = ps2.executeQuery()) {

                    // loops through each profile in the current client.
                    while (rs2.next()) {

                        rs2.getInt("profileId");
                        if (!rs2.wasNull()) {
                            Profile profile = mapProfile(rs2, client);
                            client.getProfiles().add(profile);
                        }

                    }

                    clients.add(client);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Could not get clients", e);
        }

        return clients;
    }

    @Override
    public Client getDataFromName(String name) throws Exception {
        String sql = "SELECT clientId, clientName FROM Clients WHERE clientName = ? AND deleted_at IS NULL";
        String selectProfilesSQL = """
                                       SELECT p.profileId, p.profileName,
                                              p.exportLabel, p.status, p.rotation,
                                              p.hue, p.brightness, p.contrast,
                                              p.saturation
                                       FROM Profiles p
                                       WHERE p.clientId = ? AND p.deleted_at IS NULL
                                       """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             PreparedStatement ps2 = connection.prepareStatement(selectProfilesSQL);) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Client client = mapRow(rs);
                ps2.setInt(1, client.getClientId());
                try (ResultSet rs2 = ps2.executeQuery()) {

                    while (rs2.next()) {
                        rs2.getInt("profileId");
                        if (!rs2.wasNull()) {
                            Profile profile = mapProfile(rs2, client);
                            client.getProfiles().add(profile);
                        }
                    }
                    return client;
                }
            }
            else{
                return null;
            }

        } catch (SQLException e) {
            throw new Exception("Could not fetch client from name " + name, e);
        }
    }

    @Override
    public Client getDataFromId(int id) throws Exception {
        return null;
    }

    @Override
    public void updateData(Client client) throws Exception {
        String sql = "UPDATE Clients SET clientName = ? WHERE clientId = ? AND deleted_at IS NULL";

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, client.getClientName());
            ps.setInt(2, client.getClientId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Could not update client", e);
        }
    }

    @Override
    public void deleteData(Client client) throws Exception {
        String sql = "UPDATE Clients SET deleted_at = SYSUTCDATETIME() WHERE clientId = ?";

        try (Connection connection = DBConnector.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, client.getClientId());
                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new Exception("Could not delete client", e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(rs.getInt("clientId"), rs.getString("clientName"));
    }

    private Profile mapProfile(ResultSet rs, Client tempClient) throws SQLException {

        return new Profile(
                rs.getInt("profileId"),
                tempClient,
                rs.getString("profileName"),
                ProfileStatus.valueOf(rs.getString("status")),
                rs.getString("exportLabel"),
                new FileAdjustmentSettings(
                        rs.getInt("rotation"),
                        rs.getInt("hue"),
                        rs.getInt("brightness"),
                        rs.getInt("contrast"),
                        rs.getInt("saturation"),
                        0
                )
        );

    }
}
