package ScanHub.BE;

import java.util.List;

public class Box {

    private int id;
    private String name;
    private List<Document> documents;
    private Profile profile;

    public Box(int id, String name, List<Document> documents, Profile profile) {
        this.id = id;
        this.name = name;
        this.documents = documents;
        this.profile = profile;
    }

    public Box(String name, List<Document> documents, Profile profile) {
        this.name = name;
        this.documents = documents;
        this.profile = profile;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public List<Document> getDocuments() {
        return documents;
    }
    public Profile getProfile() {
        return profile;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
