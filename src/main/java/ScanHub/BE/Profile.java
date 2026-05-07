package ScanHub.BE;

import java.util.Objects;

public class Profile {

    private int profileId;
    private int clientId;
    private Client client;
    private String profileName;
    private SplitBehavior splitBehavior;
    private ProfileStatus status;
    private String exportLabel;

    public Profile() {}

    public Profile(int profileId, int clientId, String profileName, SplitBehavior splitBehavior, ProfileStatus status, String exportLabel) {
        this.profileId = profileId;
        this.clientId = clientId;
        this.profileName = profileName;
        this.splitBehavior = splitBehavior;
        this.status = status;
        this.exportLabel = exportLabel;
    }

    public Profile(int clientId, String profileName, SplitBehavior splitBehavior, ProfileStatus status, String exportLabel) {
        this.clientId = clientId;
        this.profileName = profileName;
        this.splitBehavior = splitBehavior;
        this.status = status;
        this.exportLabel = exportLabel;
    }

    public int getProfileId() { return profileId; }
    public int getClientId() { return clientId; }
    public Client getClient() { return client; }
    public String getProfileName() { return profileName; }
    public SplitBehavior getSplitBehavior() { return splitBehavior; }
    public ProfileStatus getStatus() { return status; }
    public String getExportLabel() { return exportLabel; }

    public void setProfileId(int profileId) { this.profileId = profileId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setClient(Client client) { this.client = client; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public void setSplitBehavior(SplitBehavior splitBehavior) { this.splitBehavior = splitBehavior; }
    public void setStatus(ProfileStatus status) { this.status = status; }
    public void setExportLabel(String exportLabel) { this.exportLabel = exportLabel; }

    @Override
    public String toString() { return this.profileName; }

    @Override
    public int hashCode() { return Objects.hash("Profile_" + this.profileId); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;
        Profile other = (Profile) o;
        return Objects.equals(this.profileId, other.profileId);
    }
}