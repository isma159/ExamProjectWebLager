package ScanHub.BE;

// java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Profile {

    private int profileId;
    private String profileName;
    private SplitBehavior splitBehavior; // "NONE", "MANUAL", "BARCODE"
    private ProfileStatus status;
    private String exportLabel;
    private List<User> users;

    public Profile(int profileId, String profileName, SplitBehavior splitBehavior, ProfileStatus status, String exportLabel) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.splitBehavior = splitBehavior;
        this.status = status;
        this.exportLabel = exportLabel;
        this.users = new ArrayList<>();
    }

    public Profile(String profileName, SplitBehavior splitBehavior, ProfileStatus status, String exportLabel) {
        this.profileName = profileName;
        this.splitBehavior = splitBehavior;
        this.status = status;
        this.exportLabel = exportLabel;
        this.users = new ArrayList<>();
    }

    public int getProfileId() {
        return profileId;
    }
    public String getProfileName() {
        return profileName;
    }
    public SplitBehavior getSplitBehavior() {
        return splitBehavior;
    }
    public ProfileStatus getStatus() {return status;}
    public String getExportLabel() {return exportLabel;}
    public List<User> getUsers() {return users;}

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    public void setSplitBehavior(SplitBehavior splitBehavior) {
        this.splitBehavior = splitBehavior;
    }
    public void setStatus(ProfileStatus status) {
        this.status = status;
    }
    public void setExportLabel(String exportLabel) {this.exportLabel = exportLabel;}
    public void setUsers(List<User> users) {this.users = users;}

    @Override
    public String toString() {
        return this.profileName;
    }

    @Override
    public int hashCode() {
        return Objects.hash("Profile_" + this.profileId);
    }

    @Override
    public boolean equals(Object o) {
        // if this (object) is equal to the object given as argument, then return true
        if (this == o) return true;
        // if it's not an instance of Profile class, then it's not a profile and returns false
        if (!(o instanceof Profile)) return false;
        // if all else fails, converts given object to profile and compares with equals method from Objects class
        Profile other = (Profile) o;
        return Objects.equals(this.profileId, other.profileId);
    }
}
