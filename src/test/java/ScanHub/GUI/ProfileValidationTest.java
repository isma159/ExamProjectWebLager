package ScanHub.GUI;

// project imports
import ScanHub.BE.Client;
import ScanHub.BE.FileAdjustmentSettings;
import ScanHub.BE.Profile;
import ScanHub.BE.enums.ProfileStatus;

// library imports
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//java imports
import java.util.ArrayList;
import java.util.List;


/**
 * Unit tests for profile status validation logic.
 * Verifies that inactive profiles cannot be assigned to new users,
 * while active profiles are available for assignment.
 */
public class ProfileValidationTest {

    private Client testClient;
    private FileAdjustmentSettings defaultSettings;
    private Profile activeProfile;
    private Profile inactiveProfile;

    @BeforeEach
    void setUp() {
        testClient = new Client(1, "Test Client");
        defaultSettings = new FileAdjustmentSettings(0, 0, 0, 0, 0, 0);

        activeProfile = new Profile(
                1, testClient, "Active Profile",
                ProfileStatus.ACTIVE, "ActiveProfile_", defaultSettings
        );

        inactiveProfile = new Profile(
                2, testClient, "Inactive Profile",
                ProfileStatus.INACTIVE, "InactiveProfile_", defaultSettings
        );
    }

    /**
     * Simulates the filtering logic used in UserFormController
     * when building the profile assignment tree.
     * Inactive profiles should be excluded unless already assigned.
     */
    private List<Profile> getAssignableProfiles(List<Profile> allProfiles, List<Integer> assignedProfileIds) {
        List<Profile> assignable = new ArrayList<>();
        for (Profile profile : allProfiles) {
            boolean isAssigned = assignedProfileIds.contains(profile.getProfileId());
            if (profile.getStatus() == ProfileStatus.INACTIVE && !isAssigned) {
                continue;
            }
            assignable.add(profile);
        }
        return assignable;
    }

    @Test
    void activeProfile_ShouldBeAvailableForAssignment() {
        List<Profile> allProfiles = List.of(activeProfile);
        List<Integer> assignedIds = new ArrayList<>();

        List<Profile> result = getAssignableProfiles(allProfiles, assignedIds);

        assertTrue(result.contains(activeProfile),
                "Active profile should be available for assignment to a new user");
    }

    @Test
    void inactiveProfile_ShouldNotBeAvailableForNewAssignment() {
        List<Profile> allProfiles = List.of(inactiveProfile);
        List<Integer> assignedIds = new ArrayList<>();

        List<Profile> result = getAssignableProfiles(allProfiles, assignedIds);

        assertFalse(result.contains(inactiveProfile),
                "Inactive profile should not be available for assignment to a new user");
    }

    @Test
    void inactiveProfile_ShouldRemainVisibleIfAlreadyAssigned() {
        List<Profile> allProfiles = List.of(inactiveProfile);
        List<Integer> assignedIds = List.of(inactiveProfile.getProfileId());

        List<Profile> result = getAssignableProfiles(allProfiles, assignedIds);

        assertTrue(result.contains(inactiveProfile),
                "Inactive profile should still be visible if it was already assigned to the user");
    }

    @Test
    void mixedProfiles_ShouldOnlyReturnActiveOnesForNewUser() {
        List<Profile> allProfiles = List.of(activeProfile, inactiveProfile);
        List<Integer> assignedIds = new ArrayList<>();

        List<Profile> result = getAssignableProfiles(allProfiles, assignedIds);

        assertEquals(1, result.size(),
                "Only one profile should be assignable when one is active and one is inactive");
        assertTrue(result.contains(activeProfile),
                "The active profile should be in the result");
        assertFalse(result.contains(inactiveProfile),
                "The inactive profile should not be in the result");
    }
}