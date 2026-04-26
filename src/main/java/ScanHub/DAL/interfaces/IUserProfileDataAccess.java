package ScanHub.DAL.interfaces;

import java.util.List;

public interface IUserProfileDataAccess {

    /**
     * Assigns a list of profile IDs to a user.
     * Replaces any existing profile assignments.
     *
     * @param userId the ID of the user
     * @param profileIds list of profile IDs to assign
     * @throws Exception if something goes wrong during database operations
     */
    void assignProfiles(int userId, List<Integer> profileIds) throws Exception;

    /**
     * Retrieves all profile IDs assigned to a specific user.
     *
     * @param userId the ID of the user
     * @return list of profile IDs
     * @throws Exception if something goes wrong during database operations
     */
    List<Integer> getProfileIdsForUser(int userId) throws Exception;
}