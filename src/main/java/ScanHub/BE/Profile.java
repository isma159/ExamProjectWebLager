package ScanHub.BE;

import java.util.Objects;

public class Profile {

    private int profileId;
    private String profileName;
    private SplitBehavior splitBehavior; // "NONE", "MANUAL", "BARCODE"
    private ProfileStatus status;
    private String exportLabel;

    public Profile(int profileId, String profileName, SplitBehavior splitBehavior, ProfileStatus status, String exportLabel) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.splitBehavior = splitBehavior;
        this.status = status;
        this.exportLabel = exportLabel;
    }

    public Profile(String profileName, SplitBehavior splitBehavior, ProfileStatus status, String exportLabel) {
        this.profileName = profileName;
        this.splitBehavior = splitBehavior;
        this.status = status;
        this.exportLabel = exportLabel;
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
        // if its not an instance of Profile class, then its not a profile and returns false
        if (!(o instanceof Profile)) return false;
        // if all else fails, converts given object to profile and compares with equals method from Objects class
        Profile other = (Profile) o;
        return Objects.equals(this.profileId, other.profileId);
    }
}
