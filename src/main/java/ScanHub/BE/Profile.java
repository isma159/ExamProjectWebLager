package ScanHub.BE;

import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.interfaces.CheckTreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Profile implements CheckTreeNode {

    private int profileId;
    private int clientId;
    private Client client;
    private String profileName;
    private ProfileStatus status;
    private String exportLabel;
    private FileAdjustmentSettings fileAdjustmentSettings;
    private List<User> users = new ArrayList<>(); // users assigned to this profile

    public Profile(int profileId, Client client, String profileName, ProfileStatus status, String exportLabel, FileAdjustmentSettings fileAdjustmentSettings) {
        this.profileId = profileId;
        this.client = client;
        this.profileName = profileName;
        this.status = status;
        this.exportLabel = exportLabel;
        this.fileAdjustmentSettings = fileAdjustmentSettings;
    }

    public Profile(Client client, String profileName, ProfileStatus status, String exportLabel, FileAdjustmentSettings fileAdjustmentSettings) {
        this.client = client;
        this.profileName = profileName;
        this.status = status;
        this.exportLabel = exportLabel;
        this.fileAdjustmentSettings = fileAdjustmentSettings;
    }

    public int getProfileId() { return profileId; }
    public int getClientId() { return clientId; }
    public Client getClient() { return client; }
    public String getProfileName() { return profileName; }
    public ProfileStatus getStatus() { return status; }
    public String getExportLabel() { return exportLabel; }
    public FileAdjustmentSettings getFileAdjustmentSettings() { return fileAdjustmentSettings; }
    public List<User> getUsers() { return users; }

    public void setProfileId(int profileId) { this.profileId = profileId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setClient(Client client) { this.client = client; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public void setStatus(ProfileStatus status) { this.status = status; }
    public void setExportLabel(String exportLabel) { this.exportLabel = exportLabel; }
    public void setFileAdjustmentSettings(FileAdjustmentSettings fileAdjustmentSettings) { this.fileAdjustmentSettings = fileAdjustmentSettings; }
    public void setUsers(List<User> users) { this.users = users; }

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