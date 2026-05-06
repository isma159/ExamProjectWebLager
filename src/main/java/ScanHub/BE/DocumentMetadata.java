package ScanHub.BE;

import java.time.LocalDateTime;

public class DocumentMetadata {

    private int metadataId;
    private int boxId;
    private String profileName;
    private String boxName;
    private int documentCount;
    private int fileCount;
    private LocalDateTime boxCreatedAt;

    public DocumentMetadata() {}

    public DocumentMetadata(int metadataId, int boxId, String profileName, String boxName,
                            int documentCount, int fileCount, LocalDateTime boxCreatedAt) {
        this.metadataId = metadataId;
        this.boxId = boxId;
        this.profileName = profileName;
        this.boxName = boxName;
        this.documentCount = documentCount;
        this.fileCount = fileCount;
        this.boxCreatedAt = boxCreatedAt;
    }

    public int getMetadataId()              { return metadataId; }
    public int getBoxId()                   { return boxId; }
    public String getProfileName()          { return profileName; }
    public String getBoxName()              { return boxName; }
    public int getDocumentCount()           { return documentCount; }
    public int getFileCount()               { return fileCount; }
    public LocalDateTime getBoxCreatedAt()  { return boxCreatedAt; }

    public void setMetadataId(int metadataId)               { this.metadataId = metadataId; }
    public void setBoxId(int boxId)                         { this.boxId = boxId; }
    public void setProfileName(String profileName)          { this.profileName = profileName; }
    public void setBoxName(String boxName)                  { this.boxName = boxName; }
    public void setDocumentCount(int documentCount)         { this.documentCount = documentCount; }
    public void setFileCount(int fileCount)                 { this.fileCount = fileCount; }
    public void setBoxCreatedAt(LocalDateTime boxCreatedAt) { this.boxCreatedAt = boxCreatedAt; }

    @Override
    public String toString() {
        return "Metadata #" + metadataId + " [" + boxName + "]";
    }
}