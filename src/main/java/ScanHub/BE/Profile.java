package ScanHub.BE;

public class Profile {

    private int profileId;
    private String profileName;
    private String splitBehaviour; // "NONE", "MANUAL", "BARCODE" TODO: find out if they mean like this

    public Profile() {
    }

    public Profile(int profileId, String profileName, String splitBehaviour) {
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
    public String getSplitBehaviour() {
        return splitBehaviour;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    public void setSplitBehaviour(String splitBehaviour) {
        this.splitBehaviour = splitBehaviour;
    }

    @Override
    public String toString() {
        return this.profileName;
    }
}
