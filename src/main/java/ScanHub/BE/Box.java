package ScanHub.BE;

import ScanHub.BE.interfaces.TreeNode;

// java imports
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Box implements TreeNode {

    private int boxId;
    private String boxName;
    private int profileId;
    private Profile profile;
    private LocalDateTime createdAt;
    private List<Document> documents;
    private boolean staged = false;
    private boolean modified = false;

    public Box() { this.documents = new ArrayList<>(); }

    public Box(int boxId, String boxName, int profileId, LocalDateTime createdAt) {
        this.boxId = boxId;
        this.boxName = boxName;
        this.profileId = profileId;
        this.createdAt = createdAt;
        this.documents = new ArrayList<>();
    }

    public int getBoxId() { return boxId; }
    public String getBoxName() { return boxName; }
    public List<Document> getDocuments() { return documents; }
    public int getProfileId() { return profileId; }
    public Profile getProfile() { return profile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isStaged() { return staged; }
    public boolean isModified() { return modified; }

    public void setBoxId(int boxId) { this.boxId = boxId; }
    public void setBoxName(String boxName) { this.boxName = boxName; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }
    public void setProfileId(int profileId) { this.profileId = profileId; }
    public void setProfile(Profile profile) { this.profile = profile; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setStaged(boolean staged) { this.staged = staged; }
    public void setModified(boolean modified) { this.modified = modified; }

    public int getDocumentCount() { return documents.size(); }
    public int getFileCount() { return documents.stream().mapToInt(d -> d.getFiles().size()).sum(); }

    @Override
    public String toString() { return this.boxName; }
}
