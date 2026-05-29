package ScanHub.BLL;

// project imports
import ScanHub.BE.*;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.DAL.ApiClient.ScanResult;
import ScanHub.DAL.interfaces.IScanSource;

// library imports
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// java imports
import java.time.LocalDateTime;

class ScanManagerTest {

    @Test
    void manualSplitCreatesNewStagedDocument() throws Exception {
        Box box = new Box(1, "BOX-001", 1, LocalDateTime.now());

        Profile profile = new Profile(
                new Client("Test Client"),
                "Test Profile",
                ProfileStatus.ACTIVE,
                "Export",
                new FileAdjustmentSettings(0, 0, 0, 0, 0, 0)
        );

        box.setProfile(profile);

        Document existingDocument = new Document(10, box.getBoxId(), LocalDateTime.now());
        File existingFile = new File();
        existingDocument.getFiles().add(existingFile);
        box.getDocuments().add(existingDocument);

        ScanManager scanManager = new ScanManager(new MockScanSource(), box);

        Document newDocument = scanManager.manualSplit();

        assertEquals(2, box.getDocuments().size());
        assertSame(newDocument, box.getDocuments().get(1));
        assertSame(newDocument, scanManager.getCurrentDocument());
        assertTrue(newDocument.isStaged());
        assertTrue(newDocument.getFiles().isEmpty());
    }

    private static class MockScanSource implements IScanSource {
        @Override
        public ScanResult fetchNextScan() {
            return new ScanResult(new byte[] {1, 2, 3}, false);
        }

        @Override
        public ScanResult fetchBarcodeFile() {
            return new ScanResult(new byte[] {9, 9, 9}, true);
        }
    }
}
