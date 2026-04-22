package ScanHub.BE;

public class Page {

    private int id;
    private int referenceId;
    private int sortId;
    private byte[] imageData;
    private int fileSize;

    public Page(int id, int referenceId, int sortId, byte[] imageData, int fileSize) {
        this.id = id;
        this.referenceId = referenceId;
        this.sortId = sortId;
        this.imageData = imageData;
        this.fileSize = fileSize;
    }

    public Page(int referenceId, int sortId, byte[] imageData, int fileSize) {
        this.referenceId = referenceId;
        this.sortId = sortId;
        this.imageData = imageData;
        this.fileSize = fileSize;
    }

    public int getId() {
        return id;
    }
    public int getReferenceId() {
        return referenceId;
    }
    public int getSortId() {
        return sortId;
    }
    public byte[] getImageData() {
        return imageData;
    }
    public int getFileSize() {
        return fileSize;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }
    public void setSortId(int sortId) {
        this.sortId = sortId;
    }
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}
