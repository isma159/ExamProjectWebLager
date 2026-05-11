package ScanHub.BLL;

import ScanHub.BE.Document;
import ScanHub.DAL.DAO.DocumentDAO;

import java.util.List;

/**
 * Business-logic manager for Documents.
 *
 * Document creation during a scan session is handled by ScanManager.
 * This manager covers operations outside an active scan session, such as admin views and loading existing box contents.
 */
public class DocumentManager {

    private final DocumentDAO documentDAO;

    public DocumentManager() throws Exception {
        documentDAO = new DocumentDAO();
    }

    /**
     * Creates a new empty Document in the given box.
     */
    public Document createDocument(int boxId) throws Exception {
        return documentDAO.createDocument(boxId);
    }

    /**
     * Returns all Documents for a box, each with their Files already loaded.
     * Used to restore the state of a box when resuming a session.
     */
    public List<Document> getDocumentsWithFilesByBoxId(int boxId) throws Exception {
        return documentDAO.getDocumentsWithFilesByBoxId(boxId);
    }

    /**
     * Returns all Documents for a box without loading their Files.
     */
    public List<Document> getDocumentsByBoxId(int boxId) throws Exception {
        return documentDAO.getDocumentsByBoxId(boxId);
    }

    /**
     * Returns the number of documents in a box (not the deleted ones).
     */
    public int countDocumentsForBox(int boxId) throws Exception {
        return documentDAO.countDocumentsForBox(boxId);
    }
}