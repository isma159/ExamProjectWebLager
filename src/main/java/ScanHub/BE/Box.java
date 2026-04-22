package ScanHub.BE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Box {

    private int boxId;
    private String boxName; // TODO: <clarification needed> change to int boxNumber as well in db?
    private int profileId;
    private Profile profile;
    private LocalDateTime createdAt;
    private List<Document> documents;

    public Box() {
        this.documents = new ArrayList<>();
    }

    public Box(int boxId, String boxName, int profileId, LocalDateTime createdAt) {
        this.boxId = boxId;
        this.boxName = boxName;
        this.profileId = profileId;
        this.createdAt = createdAt;
        this.documents = new ArrayList<>();
    }

    public int getBoxId() {
        return boxId;
    }
    public String getBoxName() {
        return boxName;
    }
    public int getProfileId() {
        return profileId;
    }
    public Profile getProfile() {
        return profile;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public List<Document> getDocuments() {
        return documents;
    }

    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }
    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }
    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    //public void addDocument(Document document) {this.documents.add(document);}

    @Override
    public String toString() {
        return this.boxName;
    }
}
