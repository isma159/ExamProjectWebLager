package ScanHub.BE;

public class Profile {

    private int profileId;
    private String profileName;
    private SplitBehavior splitBehavior; // "NONE", "MANUAL", "BARCODE"

    public Profile() {
    }

    public Profile(int profileId, String profileName, SplitBehavior splitBehavior) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.splitBehavior = splitBehavior;
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

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    public void setSplitBehavior(SplitBehavior splitBehavior) {
        this.splitBehavior = splitBehavior;
    }

    @Override
    public String toString() {
        return this.profileName;
    }
}
