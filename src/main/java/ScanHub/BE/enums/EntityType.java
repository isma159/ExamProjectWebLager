package ScanHub.BE.enums;

public enum EntityType {
    BOX("box"),
    DOCUMENT("document"),
    FILE("file"),
    PROFILE("profile"),
    USER("user"),
    CLIENT("client");

    private final String label;

    EntityType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
