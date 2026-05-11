package ScanHub.GUI.controllers;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.Profile;
import ScanHub.BE.interfaces.TreeNode;
import ScanHub.BLL.ScanManager;
import ScanHub.GUI.util.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import ScanHub.GUI.models.ScanModel;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.ToggleSwitch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ScanController implements Initializable, IViewController {

    @FXML private Label lblUsername, lblRole, lblEmptyState;
    @FXML private ToggleSwitch darkMode;
    @FXML private Button btnScan, btnStop, btnRotLeft, btnRotRight, btnNewDoc, btnDelete, btnUndo, btnExport, btnZoomOut, btnZoomIn;
    @FXML private ComboBox<String> comboBoxExport;
    @FXML private FlowPane pageGrid;
    @FXML private Label lblSessionStatus, pageInfoLabel, stDocsLabel, stPagesLabel; // status bar down left
    @FXML private TreeView<TreeNode> boxTreeView;

    // Session startup popup
    @FXML private StackPane sessionPopupOverlay;
    @FXML private SearchableComboBox<Profile> comboBoxProfiles;
    @FXML private TextField txtFldBoxId;
    @FXML private Spinner<Integer> spinnerGlobalRotation;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private ScanModel scanModel;

    private final ObservableList<Document> documents = FXCollections.observableArrayList();
    private Document selectedDocument;
    private File selectedFile;

    private TreeNode draggedNode; // used for drag detection (gets nulled after drop - see the initalizeTreeView())
    private Thread scanThread; // scan loop is controlled by the volatile boolean 'scanning', not thread interruption
    private volatile boolean scanning = false; // volatile: FX-thread writes are immediately visible to scan Thread (scanThread)
    private boolean sessionActive;

    // Zoom Level stuff
    private double zoomLevel = 1.0; // default
    private static final double ZOOM_STEP = 0.15;
    private static final double ZOOM_MIN  = 0.40;
    private static final double ZOOM_MAX  = 3.00;

    @Override
    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
        initializeProfileComboBox();

        lblUsername.setText(modelFacade.getSessionModel().getCurrentUser().getUsername());
        lblRole.setText("Role: " + modelFacade.getSessionModel().getCurrentUser().getRole().toString());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTreeView(boxTreeView);
        initializeKeyboardShortcuts();
        initializeExportComboBoxes();
        spinnerGlobalRotation.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-270, 270, 0, 90));

        setSessionControlsDisabled(true);
        lblSessionStatus.setText("Press Session Startup to configure and begin.");
        refreshStatusBar();
        updatePageInfoLabel();
    }

    private void initializeProfileComboBox() {
        if (modelFacade == null) return;
        comboBoxProfiles.setItems(modelFacade.getProfileModel().getProfiles());
        if (!comboBoxProfiles.getItems().isEmpty()) {
            comboBoxProfiles.getSelectionModel().selectFirst();
        }
    }

    // TODO: implement export - for now does nothing
    private void initializeExportComboBoxes() {
        comboBoxExport.setItems(FXCollections.observableArrayList("Single-page TIFF", "Multi-page TIFF"));
        comboBoxExport.getSelectionModel().selectFirst();
    }

    /**
     * Initializes the left-panel tree view with icons for Box, Documents, and Files,
     * drag-and-drop functions, reordering, and auto-expand on structural changes.
     *
     * todo explain with inline comments
     */
    private void initializeTreeView(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> root = new TreeItem<>();
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        treeView.getRoot().addEventHandler(TreeItem.childrenModificationEvent(), e -> expandAll(treeView.getRoot()));
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> onTreeSelectionChanged(newValue));

        treeView.setCellFactory(tv -> new TreeCell<>() {
            {
                setOnDragDetected(event -> {
                    TreeItem<TreeNode> item = getTreeItem();
                    if (item == null || item == treeView.getRoot()) return;

                    draggedNode = item.getValue();

                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(draggedNode != null ? draggedNode.toString() : "");
                    db.setContent(content);
                    event.consume();
                });

                setOnDragOver(event -> {
                    if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                setOnDragEntered(event -> {
                    if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                        setOpacity(0.6);
                    }
                });

                setOnDragExited(event -> setOpacity(1.0));

                setOnDragDropped(event -> {
                    TreeItem<TreeNode> targetItem = getTreeItem();
                    if (targetItem == null || targetItem.getValue() == null || draggedNode == null) return;

                    TreeNode target = targetItem.getValue();
                    boolean success = false;

                    if (draggedNode instanceof File draggedFile && target instanceof Document targetDoc) {
                        success = moveFileToDocument(draggedFile, targetDoc);
                    } else if (draggedNode instanceof File draggedFile && target instanceof File targetFile) {
                        success = moveFileBefore(draggedFile, targetFile);
                    } else if (draggedNode instanceof Document draggedDoc && target instanceof Document targetDoc) {
                        success = reorderDocument(draggedDoc, targetDoc);
                    }

                    draggedNode = null;
                    event.setDropCompleted(success);
                    event.consume();
                    if (success) rebuild();
                });
            }

            @Override
            protected void updateItem(TreeNode object, boolean empty) {
                super.updateItem(object, empty);
                if (empty || object == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                Label icon = new Label();
                icon.getStyleClass().add("icon");

                if (object instanceof Document document) {
                    icon.setText("\ue963");
                    icon.getStyleClass().add("tree-cell-doc");
                    setText(documentLabel(document));
                    setStyle(document.isStaged() || document.isModified() ? "-fx-font-weight: bold;" : ""); // styling of text for whether they are staged, modified or persisted
                } else if (object instanceof File file) {
                    icon.setText("\ue958");
                    icon.getStyleClass().add("tree-cell-file");
                    setText(fileLabel(file));
                    setStyle(file.isStaged() ? "-fx-font-weight: bold;" : ""); // styling of text for whether they are staged or persisted
                }

                setGraphic(icon);
            }
        });
    }

    public void expandAll(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandAll(child);
            }
        }
    }

    /**
     * TODO: Create shortcuts - only a template from the demo
     * ---- Ideas  ----
     * Arrows up and down: move through the Tree
     * Ctrl + c: copy a file or document with all files
     * Ctrl + v: past a file or document with all files
     * Ctrl + z: undo
     * Backspace: delete a file or document with all files (lidt voldsomt måske men idk)
     */
    private void initializeKeyboardShortcuts() {
        pageGrid.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;

            scene.setOnKeyPressed(e -> {
                KeyCode code = e.getCode();
                if      (code == KeyCode.SPACE)         { onScan(null);                  e.consume(); }
                else if (code == KeyCode.LEFT)          { onNavPrev(null);               e.consume(); }
                else if (code == KeyCode.RIGHT)         { onNavNext(null);               e.consume(); }
                else if (code == KeyCode.HOME)          { onNavFirst(null);              e.consume(); }
                else if (code == KeyCode.END)           { onNavLast(null);               e.consume(); }
                else if (code == KeyCode.OPEN_BRACKET)  { onRotateLeft(null);            e.consume(); }
                else if (code == KeyCode.CLOSE_BRACKET) { onRotateRight(null);           e.consume(); }
                else if (code == KeyCode.DELETE)        { onDeleteFileOrDocument(null);            e.consume(); }
                else if (code == KeyCode.N && !e.isControlDown()) { onNewDocument(null); e.consume(); }
                else if (code == KeyCode.E && e.isControlDown())  { onExport(null);      e.consume(); }
            });
        });
    }

    // Session Startup popup
    @FXML
    private void onSessionStartup(ActionEvent e) {
        initializeProfileComboBox();
        sessionPopupOverlay.setVisible(true);
    }

    @FXML private void onSessionPopupClose(ActionEvent e) { sessionPopupOverlay.setVisible(false); }
    @FXML private void onSessionPopupBackdropClick(MouseEvent e) { sessionPopupOverlay.setVisible(false); }
    @FXML private void onSessionPopupConsumeClick(MouseEvent e) { e.consume(); }

    @FXML
    private void onStartSession(ActionEvent e) {
        Profile profile = comboBoxProfiles.getValue();
        String boxInput = txtFldBoxId.getText().trim();

        if (profile == null) {
            AlertHelper.showError("Session Setup", "Please select a profile before starting.");
            return;
        }
        if (boxInput.isEmpty()) {
            AlertHelper.showError("Session Setup", "Please enter a Box ID before starting.");
            return;
        }

        try {
            Box activeBox = modelFacade.getBoxModel().getOrCreateSessionBox(boxInput, profile);
            scanModel = new ScanModel(activeBox);
            syncDocumentsFromModel();

            sessionActive = true;
            selectedDocument = null;
            selectedFile = null;

            setSessionControlsDisabled(false);
            lblSessionStatus.setText("Profile: " + profile.getProfileName() + " – Box: " + activeBox.getBoxName());
            sessionPopupOverlay.setVisible(false);
            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Session Setup", "Could not start scan session.");
        }
    }

    /**
     * TODO: explain
     */
    @FXML
    private void onScan(ActionEvent e) { //
        if (!sessionActive || scanModel == null || scanning) return;

        scanning = true;
        btnScan.setDisable(true);
        btnStop.setDisable(false);

        // TODO move global rotation in profile default, not on session start
        int rotation = spinnerGlobalRotation.getValue(); // gets and uses the rotation for the duration of the loop

        scanThread = new Thread(() -> {
            while (scanning) {
                try {
                    ScanManager.StoredScan result = scanModel.fetchScan(rotation);

                    Platform.runLater(() -> {
                        syncDocumentsFromModel();
                        if (result.barcodeSplit()) {
                            selectedDocument = result.document();
                            selectedFile = null;
                        } else {
                            selectPage(result.document(), result.file());
                        }
                        rebuild();
                    });

                } catch (Exception ex) {
                    scanning = false; // stop loop
                    Platform.runLater(() -> {
                        btnScan.setDisable(false);
                        ex.printStackTrace();
                        AlertHelper.showError("Scan Failed", "Scanning stopped. Could not fetch the next page. Please try again.");
                    });
                    return;
                }
            }

            // loop exited cleanly (onStop was pressed) and last fetchScan() has finished
            Platform.runLater(() -> {
                btnScan.setDisable(false);
                btnStop.setDisable(true);
            });
        });

        scanThread.setDaemon(true); // won't block app shutdown if left running
        scanThread.start(); // launch thread
    }

    /** Stops automatic scanning */
    @FXML
    private void onStop(ActionEvent e) {
        if (!scanning || !sessionActive || scanModel == null) return;

        scanning = false; // signals the loop to exit after the current fetch completes
        btnStop.setDisable(true);
    }

    @FXML
    private void onNewDocument(ActionEvent e) {
        if (!sessionActive || scanModel == null) return; // TODO keep until I make sure the shortcuts dont bypass disabled buttons

        try {
            selectedDocument = scanModel.manualSplit();
            selectedFile = null;
            syncDocumentsFromModel();
            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("New Document Failed", "Could not create a new document. Please try again.");
        }
    }

    @FXML
    private void onDeleteFileOrDocument(ActionEvent e) {
        if (scanModel == null || !sessionActive) return; // TODO keep scanModel == null until I make sure the shortcuts dont bypass disabled buttons

        try {
            if (selectedFile != null) {
                int deletedIndex = currentPageIndex();
                Document ownerDocument = findOwnerDocument(selectedFile);
                scanModel.deleteFile(selectedFile);

                if (ownerDocument != null && !ownerDocument.isStaged()) { ownerDocument.setModified(true); } // document has been changed

                documents.removeIf(document -> document.getFiles().isEmpty()); // removes document if it is empty

                // Select nearest page, or clear selection if nothing left
                List<File> remaining = allPages();
                if (remaining.isEmpty()) {
                    selectedDocument = null;
                    selectedFile = null;
                } else {
                    int next = Math.min(deletedIndex, remaining.size() - 1);
                    selectedFile = remaining.get(Math.max(next, 0));
                    selectedDocument = findOwnerDocument(selectedFile);
                }
            } else if (selectedDocument != null) {
                scanModel.deleteDocument(selectedDocument);
                documents.remove(selectedDocument);
                selectedDocument = null;
                selectedFile = null;
            } else return;

            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Delete Failed", "Could not delete the selected "
                    + (selectedFile != null ? "page." : "document.") + " Please try again.");
        }
    }

    @FXML private void onRotateLeft(ActionEvent e)  { rotatePage(-90); }
    @FXML private void onRotateRight(ActionEvent e) { rotatePage(90); }

    private void rotatePage(int degrees) {
        if (selectedFile == null || scanModel == null) return; // TODO keep scanModel == null until I make sure the shortcuts dont bypass disabled buttons

        int rotation = normaliseRotation(selectedFile.getRotation() + degrees);
        try {
            scanModel.updateFileRotation(selectedFile, rotation);
            rebuildPreviewCard();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Rotation Failed", "Could not update page rotation.");
        }
    }

    // findes ikke længere men bring tilbage måske?
    @FXML private void onNavFirst(ActionEvent e) { navigateTo(0); }
    @FXML private void onNavPrev(ActionEvent e) { navigateTo(currentPageIndex() - 1); }
    @FXML private void onNavNext(ActionEvent e) { navigateTo(currentPageIndex() + 1); }
    @FXML private void onNavLast(ActionEvent e) { navigateTo(allPages().size() - 1); }

    /**
     * todo explain
     * @param index
     */
    private void navigateTo(int index) {
        List<File> all = allPages();
        if (all.isEmpty()) return;
        index = Math.max(0, Math.min(index, all.size() - 1));
        File target = all.get(index);
        for (Document document : documents) {
            if (document.getFiles().contains(target)) {
                selectPage(document, target);
                break;
            }
        }
        rebuildPreviewCard();
    }

    @FXML
    private void onZoomIn(ActionEvent e) {
        zoomLevel = Math.min(zoomLevel + ZOOM_STEP, ZOOM_MAX);
        rebuildPreviewCard();
    }

    @FXML
    private void onZoomOut(ActionEvent e) {
        zoomLevel = Math.max(zoomLevel - ZOOM_STEP, ZOOM_MIN);
        rebuildPreviewCard();
    }

    /**
     * Export
     * TODO export to multi- or single page tiff on pc (for now it only saves in db)
     */
    @FXML
    private void onExport(ActionEvent e) {
        if (!sessionActive || scanModel == null || scanning) return;

        if (documents.isEmpty() || totalPageCount() == 0) {
            AlertHelper.showError("Export", "There are no documents to export.");
            return;
        }

        try {
            scanModel.save();
            // TODO
            /* FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Export");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));
            fileChooser.setInitialFileName(""); // <- set as boxmetadata */

            rebuild();

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Export Failed", "Could not save and export pages and documents. Please try again.");
        }
    }

    @FXML
    private void onUndo(ActionEvent e) {
        // TODO: implement undo
    }

    @FXML
    private void onDarkModeToggle() { ThemeManager.toggle(currentStage.getScene(), darkMode.isSelected()); }

    /**
     * For now logs out
     * TODO: Exit to previous window - since user and admin has their respective views later, it will be implemented by then
     * @param actionEvent
     */
    @FXML
    private void onExit(ActionEvent actionEvent) {
        AlertHelper.showConfirmation("Exit Window", "Are you sure you want to exit?\n"
                + "Any unsaved progress will be deleted.", () -> {
            try {
                ViewHandler handler = ViewHandler.LOGIN;
                handler.reset();
                handler.show(modelFacade);
                modelFacade.getSessionModel().logout();
                currentStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Exit Error", "Failed to exit window. Please try again.");
            }
        });
    }

    /** todo explain */
    private void onTreeSelectionChanged(TreeItem<TreeNode> item) {
        if (item == null || item.getParent() == null || item.getValue() == null) return;

        TreeNode value = item.getValue();
        if (value instanceof Document document) {
            selectedDocument = document;
            selectedFile = null;
        } else if (value instanceof File file) {
            for (Document document : documents) {
                if (document.getFiles().contains(file)) {
                    selectPage(document, file);
                    break;
                }
            }
        }

        rebuildPreviewCard();
    }

    // Drag and drop
    /**
     * todo explain with inline comments
     */
    private boolean moveFileToDocument(File file, Document target) {
        Document source = findOwnerDocument(file);
        if (source == null || source == target) return false;

        source.getFiles().remove(file);
        target.getFiles().add(file);
        persistFileMoved(file, target);
        return true;
    }

    /**
     * todo explain with inline comments
     */
    private boolean moveFileBefore(File dragged, File targetFile) {
        if (dragged == targetFile) return false;

        Document draggedOwner = findOwnerDocument(dragged);
        Document targetOwner  = findOwnerDocument(targetFile);
        if (draggedOwner == null || targetOwner == null) return false;

        draggedOwner.getFiles().remove(dragged);
        int insertIndex = targetOwner.getFiles().indexOf(targetFile);
        targetOwner.getFiles().add(insertIndex, dragged);
        persistFileMoved(dragged, targetOwner);
        return true;
    }

    /**
     * todo explain with inline comments
     */
    private boolean reorderDocument(Document dragged, Document target) {
        if (dragged == target) return false;

        documents.remove(dragged);
        int insertIndex = documents.indexOf(target);
        documents.add(insertIndex, dragged);
        return true;
    }

    /**
     * todo explain
     */
    private void persistFileMoved(File file, Document newOwner) {
        try {
            modelFacade.getFileModel().moveFile(file, newOwner.getDocumentId());
        } catch (Exception e) {
            AlertHelper.showError("Move Failed", "Could not persist file move: " + e.getMessage());
        }
    }

    /**
     * todo explain
     */
    private Document findOwnerDocument(File file) {
        return documents.stream().filter(document -> document.getFiles().contains(file)).findFirst().orElse(null);
    }

    // Rebuild/refresh
    private void rebuild() {
        rebuildPreviewCard();
        refreshTree();
        refreshStatusBar();
    }

    /**
     * todo explain with inline comments
     */
    private void rebuildPreviewCard() {
        pageGrid.getChildren().clear();

        File previewFile = (selectedFile == null && selectedDocument != null && !selectedDocument.getFiles().isEmpty())
                ? selectedDocument.getFiles().getFirst() : selectedFile;

        if (selectedDocument != null && previewFile != null) {
            pageGrid.getChildren().add(buildPageCard(selectedDocument, previewFile));
        }

        updatePageInfoLabel();
        lblEmptyState.setVisible(previewFile == null);
    }

    /**
     * todo explain with inline comments
     */
    private void refreshTree() {
        TreeItem<TreeNode> root = boxTreeView.getRoot();
        root.getChildren().clear();

        for (Document document : documents) {
            TreeItem<TreeNode> docItem = new TreeItem<>(document);
            docItem.setExpanded(true);
            for (File file : document.getFiles()) {
                docItem.getChildren().add(new TreeItem<>(file));
            }
            root.getChildren().add(docItem);
        }
    }

    private void refreshStatusBar() {
        stDocsLabel.setText("Documents: " + documents.size());
        stPagesLabel.setText("Pages: " + totalPageCount());
    }

    private void updatePageInfoLabel() {
        List<File> all = allPages();
        int index = currentPageIndex();
        if (all.isEmpty() || index < 0) {
            pageInfoLabel.setText("0 / 0");
            return;
        }
        pageInfoLabel.setText((index + 1) + " / " + all.size());
    }

    /**
     * TODO: center this and see what else I can do
     * @param document
     * @param file
     * @return
     */
    private VBox buildPageCard(Document document, File file) {
        double cw = cardWidth();
        double ch = cardHeight();

        ImageView thumb = new ImageView();
        if (file.getImageData() != null) {
            Image image = createPreviewImage(file.getImageData(), cw - 8, ch - 44);
            if (!image.isError()) {
                thumb.setImage(image);
            }
        }
        thumb.setFitWidth(cw - 8);
        thumb.setFitHeight(ch - 44);
        thumb.setPreserveRatio(true);

        Label nameLabel = new Label(fileLabel(file));
        nameLabel.getStyleClass().add("lbl");
        nameLabel.setMaxWidth(cw - 8);

        Label docLabel = new Label(documentLabel(document));
        docLabel.getStyleClass().add("lbl");
        docLabel.setMaxWidth(cw - 8);
        docLabel.setStyle("-fx-font-size:9;");

        VBox card = new VBox(4, thumb, nameLabel, docLabel);
        card.setPrefWidth(cw);
        card.setPrefHeight(ch);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("card", "card-bg", "shadow");
        card.setPadding(new Insets(4));
        card.setRotate(file.getRotation());
        card.setUserData(file);
        card.setOnMouseClicked(event -> {
            selectPage(document, file);
            updatePageInfoLabel();
        });

        return card;
    }

    /**
     * todo explain with inline comments
     * @param imageData
     * @param width
     * @param height
     * @return
     */
    private Image createPreviewImage(byte[] imageData, double width, double height) {
        try {
            ImageIO.scanForPlugins();
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (bufferedImage != null) {
                ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", pngOut);
                return new Image(new ByteArrayInputStream(pngOut.toByteArray()), width, height, true, true);
            }
        } catch (Exception ignored) {}

        return new Image(new ByteArrayInputStream(imageData), width, height, true, true);
    }

    private double cardWidth() { return 520 * zoomLevel; }
    private double cardHeight() { return 700 * zoomLevel; }

    private void selectPage(Document document, File file) {
        selectedDocument = document;
        selectedFile = file;
    }

    /**  Syncs the observable list(documents) from the model's in-memory box state  */
    private void syncDocumentsFromModel() { documents.setAll(scanModel.getTargetBox().getDocuments()); }

    private List<File> allPages() {
        List<File> files = new ArrayList<>();
        for (Document document : documents) {
            files.addAll(document.getFiles());
        }
        return files;
    }

    private int totalPageCount() { return documents.stream().mapToInt(document -> document.getFiles().size()).sum(); }

    private void setSessionControlsDisabled(boolean disabled) {
        btnScan.setDisable(disabled);
        btnStop.setDisable(disabled);
        btnRotLeft.setDisable(disabled);
        btnRotRight.setDisable(disabled);
        btnNewDoc.setDisable(disabled);
        btnDelete.setDisable(disabled);
        btnUndo.setDisable(disabled);
        btnExport.setDisable(disabled);
        btnZoomOut.setDisable(disabled);
        btnZoomIn.setDisable(disabled);
    }

    /** Labels documents */
    private String documentLabel(Document document) {
        int position = documents.indexOf(document) + 1;
        return "Document " + position;
    }

    /** Labels files */
    private String fileLabel(File file) {
        for (Document doc : documents) {
            int position = doc.getFiles().indexOf(file);
            if (position >= 0) {
                return "Page " + (position + 1);
            }
        }
        return "Page ?"; // something is wrong
    }

    /**
     * TODO explain
     * @return
     */
    private int currentPageIndex() {
        if (selectedFile == null) return -1;
        return allPages().indexOf(selectedFile);
    }

    /**
     * TODO: change to "rotation % 360" only if we remove left rotation
     * @param rotation
     * @return
     */
    private int normaliseRotation(int rotation) {
        int normalised = ((rotation % 360) + 360) % 360;
        return switch (normalised) {
            case 90, 180, 270 -> normalised;
            default -> 0;
        };
    }
}
