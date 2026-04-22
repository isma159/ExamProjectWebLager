package ScanHub.BE;

import java.util.List;

public class Document {

    private int id;
    private List<Page> pages;
    private String status;

    public Document(int id, List<Page> pages, String status) {
        this.id = id;
        this.pages = pages;
        this.status = status;
    }

    public Document(List<Page> pages, String status) {
        this.pages = pages;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    public List<Page> getPages() {
        return pages;
    }
    public String getStatus() {
        return status;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
