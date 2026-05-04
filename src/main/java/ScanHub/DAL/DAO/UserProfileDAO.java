package ScanHub.DAL.DAO;

// project imports
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IUserProfileDataAccess;

// java imports
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserProfileDAO implements IUserProfileDataAccess {

    DBConnector dbConnector = new DBConnector();

    public UserProfileDAO() throws IOException {
    }

    public void assignProfiles(int userId, List<Integer> profileIds) throws Exception {

        try (Connection connection = dbConnector.getConnection()) {
            connection.setAutoCommit(false); // disable auto commit so multiple SQL operations are treated as one transaction (nothing is saved yet)

            try (PreparedStatement deletePS = connection.prepareStatement("DELETE FROM UserProfiles WHERE userId = ?");
                 PreparedStatement insertPS = connection.prepareStatement("INSERT INTO UserProfiles (userId, profileId) VALUES (?, ?)")) {

                // delete existing profiles
                deletePS.setInt(1, userId);
                deletePS.executeUpdate();

                // insert new profiles
                for (Integer profileId : profileIds) {
                    insertPS.setInt(1, userId);
                    insertPS.setInt(2, profileId);
                    insertPS.addBatch();
                }

                insertPS.executeBatch();

                connection.commit(); // permanently save all changes (commit) after disabling auto commit (everything succeeds)

            } catch (SQLException e) {
                connection.rollback(); // if something fails rollback (revert) back to before
                throw e;
            }

        } catch (SQLException e) {
            throw new Exception("Could not assign profiles to user", e);
        }
    }

    public List<Integer> getProfileIdsForUser(int userId) throws Exception {
        List<Integer> profileIds = new ArrayList<>();

        try (Connection connection = dbConnector.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT profileId FROM UserProfiles WHERE userId = ?");

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int profileId = rs.getInt("profileId");
                profileIds.add(profileId);
            }

        } catch (SQLException e) {
            throw new Exception("Could not get profiles for users", e);
        }

        return profileIds;
    }
}
