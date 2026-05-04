package ScanHub.BE;

import java.time.LocalDateTime;

public class DocumentMetadata {

    private int metadataId;
    private int documentId;
    private String title;
    private String documentType;   // e.g. "Contract", "Invoice", "Letter"
    private String referenceNumber;
    private String author;
    private String notes;
    private LocalDateTime documentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DocumentMetadata() {}

    public DocumentMetadata(int metadataId, int documentId, String title, String documentType,
                            String referenceNumber, String author, String notes,
                            LocalDateTime documentDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.metadataId = metadataId;
        this.documentId = documentId;
        this.title = title;
        this.documentType = documentType;
        this.referenceNumber = referenceNumber;
        this.author = author;
        this.notes = notes;
        this.documentDate = documentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getMetadataId()             { return metadataId; }
    public int getDocumentId()             { return documentId; }
    public String getTitle()               { return title; }
    public String getDocumentType()        { return documentType; }
    public String getReferenceNumber()     { return referenceNumber; }
    public String getAuthor()              { return author; }
    public String getNotes()               { return notes; }
    public LocalDateTime getDocumentDate() { return documentDate; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getUpdatedAt()    { return updatedAt; }

    public void setMetadataId(int metadataId)             { this.metadataId = metadataId; }
    public void setDocumentId(int documentId)             { this.documentId = documentId; }
    public void setTitle(String title)                    { this.title = title; }
    public void setDocumentType(String documentType)      { this.documentType = documentType; }
    public void setReferenceNumber(String referenceNumber){ this.referenceNumber = referenceNumber; }
    public void setAuthor(String author)                  { this.author = author; }
    public void setNotes(String notes)                    { this.notes = notes; }
    public void setDocumentDate(LocalDateTime documentDate){ this.documentDate = documentDate; }
    public void setCreatedAt(LocalDateTime createdAt)     { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)     { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Metadata #" + metadataId + " [" + title + "]";
    }
}