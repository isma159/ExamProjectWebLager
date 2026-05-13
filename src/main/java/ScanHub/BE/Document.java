package ScanHub.BE;

import ScanHub.BE.interfaces.TreeNode;

// java imports
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Document implements TreeNode {

    private int documentId;
    private int boxId;
    private LocalDateTime createdAt;
    private List<File> files;
    private boolean staged = false;
    private boolean modified = false;

    public Document() {
        this.files = new ArrayList<>();
    }

    public Document(int documentId, int boxId, LocalDateTime createdAt) {
        this.documentId = documentId;
        this.boxId = boxId;
        this.createdAt = createdAt;
        this.files = new ArrayList<>();
    }

    public int getDocumentId() { return documentId; }
    public int getBoxId() { return boxId; }
    public List<File> getFiles() { return files; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isStaged() { return staged; }
    public boolean isModified() { return modified; }

    public void setDocumentId(int documentId) { this.documentId = documentId; }
    public void setBoxId(int boxId) { this.boxId = boxId; }
    public void setFiles(List<File> files) { this.files = files; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setStaged(boolean staged) { this.staged = staged; }
    public void setModified(boolean modified) { this.modified = modified; }

    @Override
    public String toString() {
        return "Document #" + this.documentId;
    }

    @Override
    public String getDisplayName() {
        return "\uE963" + " " + "Document #" + this.documentId;
    }
}
