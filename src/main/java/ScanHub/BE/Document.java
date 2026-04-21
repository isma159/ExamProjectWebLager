package ScanHub.BE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Document {

    private int documentId;
    private int boxId;
    private String status; // "IN_PROGRESS", "PENDING_QA", "QA_COMPLETED"
    private LocalDateTime createdAt;
    private List<File> files;

    public Document() {
        this.files = new ArrayList<>();
    }

    public Document(int documentId, int boxId, String status, LocalDateTime createdAt) {
        this.documentId = documentId;
        this.boxId = boxId;
        this.status = status;
        this.createdAt = createdAt;
        this.files = new ArrayList<>();
    }

    public int getDocumentId() {
        return documentId;
    }
    public int getBoxId() {
        return boxId;
    }
    public String getStatus() {
        return status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public List<File> getFiles() {
        return files;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }
    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setFiles(List<File> files) {
        this.files = files;
    }

    //public void addFile(File file) {this.files.add(file);}

    @Override
    public String toString() {
        return "Document #" + this.documentId;
    }
}
