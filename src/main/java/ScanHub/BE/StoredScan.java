package ScanHub.BE;

// record returned to controller after each successful scan
public record StoredScan(File file, Document document, boolean barcodeSplit) {}
