package ScanHub.BE.enums;

public enum ExportMode {
    SinglePageTIFF("Single-Page TIFF"),
    MultiPageTIFF("Multi-Page TIFF");

    private final String label;

    ExportMode(String label) { this.label = label; }

    @Override
    public String toString() {
        return label;
    }
}
