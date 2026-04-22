package ScanHub.BE;

public class Profile {

    private int profileId;
    private String profileName;
    private SplitBehavior splitBehaviour; // "NONE", "MANUAL", "BARCODE" TODO: find out if they mean like this

    public Profile() {
    }

    public Profile(int profileId, String profileName, SplitBehavior splitBehaviour) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.splitBehaviour = splitBehaviour;
    }

    public int getProfileId() {
        return profileId;
    }
    public String getProfileName() {
        return profileName;
    }
    public SplitBehavior getSplitBehaviour() {
        return splitBehaviour;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    public void setSplitBehaviour(SplitBehavior splitBehaviour) {
        this.splitBehaviour = splitBehaviour;
    }

    @Override
    public String toString() {
        return this.profileName;
    }
}
