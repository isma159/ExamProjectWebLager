package ScanHub.BE;

import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.interfaces.CheckTreeNode;

import java.util.Objects;

public class Profile implements CheckTreeNode {

    private int profileId;
    private int clientId;
    private Client client;
    private String profileName;
    private ProfileStatus status;
    private String exportLabel;
    private FileSettings fileSettings;

    public Profile(int profileId, Client client, String profileName, ProfileStatus status, String exportLabel, FileSettings fileSettings) {
        this.profileId = profileId;
        this.client = client;
        this.profileName = profileName;
        this.status = status;
        this.exportLabel = exportLabel;
        this.fileSettings = fileSettings;
    }

    public Profile(Client client, String profileName, ProfileStatus status, String exportLabel, FileSettings fileSettings) {
        this.client = client;
        this.profileName = profileName;
        this.status = status;
        this.exportLabel = exportLabel;
        this.fileSettings = fileSettings;
    }

    public int getProfileId() { return profileId; }
    public int getClientId() { return clientId; }
    public Client getClient() { return client; }
    public String getProfileName() { return profileName; }
    public ProfileStatus getStatus() { return status; }
    public String getExportLabel() { return exportLabel; }
    public FileSettings getFileSettings() { return fileSettings; }

    public void setProfileId(int profileId) { this.profileId = profileId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setClient(Client client) { this.client = client; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public void setStatus(ProfileStatus status) { this.status = status; }
    public void setExportLabel(String exportLabel) { this.exportLabel = exportLabel; }
    public void setFileSettings(FileSettings fileSettings) { this.fileSettings = fileSettings; }

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