package ScanHub.GUI.models;

import ScanHub.BE.File;
import ScanHub.BLL.FileManager;

public class FileModel {

    private final FileManager fileManager;

    public FileModel() throws Exception {
        this.fileManager = new FileManager();
    }

    public void moveFile(File file, int newDocumentId) throws Exception {
        fileManager.moveFile(file, newDocumentId);
    }
}