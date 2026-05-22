package ScanHub.GUI.controllers;

import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.ExportMode;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.interfaces.TreeNode;
import ScanHub.BLL.ScanManager;
import ScanHub.GUI.util.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import ScanHub.GUI.models.ScanModel;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;
import com.sun.source.tree.Tree;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.ToggleSwitch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ScanController implements Initializable, IViewController {

    @FXML private BorderPane workspaceView;
    @FXML private Label lblUsername, lblRole, lblEmptyState;
    @FXML private ToggleSwitch darkMode;
    @FXML private Button btnScan, btnStop, btnRotLeft, btnRotRight, btnNewDoc, btnSplitDoc, btnDelete, btnUndo, btnExport, btnZoomOut, btnZoomIn;
    @FXML private ComboBox<ExportMode> comboBoxExport;
    @FXML private FlowPane pageGrid;
    @FXML private Label lblSessionStatus, pageInfoLabel, stDocsLabel, stPagesLabel;
    @FXML private TreeView<TreeNode> boxTreeView;

    // Session startup popup
    @FXML private StackPane sessionPopupOverlay;
    @FXML private SearchableComboBox<Profile> comboBoxProfiles;
    @FXML private TextField txtFldBoxId, txtFldGlobalRotation, txtFldGlobalHue, txtFldGlobalBrightness, txtFldGlobalContrast, txtFldGlobalSaturation;

    // File adjustment menu
    @FXML private StackPane sessionPopupOverlay1;
    @FXML private TextField txtFldIndividualFileRotation, txtFldIndividualFileHue, txtFldIndividualFileBrightness, txtFldIndividualFileContrast, txtFldIndividualFileSaturation;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private ScanModel scanModel;
    private TreeItem<TreeNode> root = new TreeItem<>();
    private final ObservableList<Document> documents = FXCollections.observableArrayList();
    private Document selectedDocument;
    private File selectedFile;
    private Box selectedBox;
    private boolean sessionActive;

    private TreeNode draggedNode; // used for drag detection (gets nulled after drop)
    private Thread scanThread; // scan loop is controlled by the volatile boolean 'scanning', not thread interruption
    private volatile boolean scanning = false; // volatile: FX-thread writes are immediately visible to the scan thread
    private static final long scanDelay = 2000; // milliseconds to wait between successive scans in the scan loop

    // Zoom Level stuff
    private double zoomLevel = 1.0; // default
    private static final double ZOOM_STEP = 0.15;
    private static final double ZOOM_MIN  = 0.40;
    private static final double ZOOM_MAX  = 3.00;

    private final ChangeListener<TreeItem<TreeNode>> treeSelectionListener =
            (obs, oldValue, newValue) -> onTreeSelectionChanged(newValue);
    @FXML
    private Spinner<Integer> spinnerRotation;

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
        initializeKeyboardShortcuts();
        initializeExportComboBoxes();
        spinnerRotation.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 270, 90, 90));
        comboBoxProfiles.valueProperty().addListener((obs, oldValue, newValue) -> updateProfileAdjustmentsFields(newValue));

        setSessionControlsDisabled(true);
        lblSessionStatus.setText("Press Session Startup to configure and begin.");
        refreshStatusBar();
        updatePageInfoLabel();
    }

    private void initializeProfileComboBox() {
        if (modelFacade == null) return;
        User user = modelFacade.getSessionModel().getCurrentUser();
        if (user.getProfiles().isEmpty() && !user.isAdmin()) return;

        if (user.isAdmin()) { user.setProfiles(modelFacade.getProfileModel().getProfiles()); }

        comboBoxProfiles.setItems(FXCollections.observableArrayList(user.getProfiles()));
    }

    private void initializeExportComboBoxes() {
        comboBoxExport.setItems(FXCollections.observableArrayList(ExportMode.values()));
        comboBoxExport.getSelectionModel().selectFirst();
    }

    /**
     * Initializes the left-panel tree view with icons for Box, Documents, and Files,
     * drag-and-drop functions, reordering, and auto-expand on structural changes.
     *
     * todo explain with inline comments
     */
    private void initializeTreeView(TreeView<TreeNode> treeView, Box rootBox) {
        TreeItem<TreeNode> root = new TreeItem<>(rootBox);
        root.getChildren().clear();
        treeView.setRoot(root);
        treeView.setShowRoot(true);

        treeView.getRoot().addEventHandler(TreeItem.childrenModificationEvent(), e -> expandAll(treeView.getRoot()));
        treeView.getSelectionModel().selectedItemProperty().addListener(treeSelectionListener);

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
                if (object instanceof Box box) {
                    icon.setText("\ue9d9");
                    icon.getStyleClass().add("tree-cell-box");
                    setText(comboBoxProfiles.getValue().getExportLabel() + box.getBoxName());
                    setStyle(box.isStaged() ? "-fx-font-weight: bold;" : "");
                } else if (object instanceof Document document) {
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

    private void initializeKeyboardShortcuts() {
        pageGrid.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;

            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case SPACE -> {
                        if (!scanning) onScan(null);
                        else onStop(null);
                        e.consume();
                    }
                    case LEFT -> {
                        if (e.isControlDown()) {
                            onRotateLeft(null);
                        } else {
                            onNavPrev(null);
                        }
                        e.consume();
                    }
                    case RIGHT -> {
                        if (e.isControlDown()) {
                            onRotateRight(null);
                        } else {
                            onNavNext(null);
                        }
                        e.consume();
                    }
                    case PAGE_UP -> {
                        onNavFirst(null);
                        e.consume();
                    }
                    case PAGE_DOWN -> {
                        onNavLast(null);
                        e.consume();
                    }
                    case DELETE -> {
                        onDeleteFileOrDocument(null);
                        e.consume();
                    }
                    case N -> {
                        if (e.isControlDown()) {
                            onNewDocument(null);
                            e.consume();
                        }
                    }
                    case E -> {
                        if (e.isControlDown()) {
                            onExport(null);
                            e.consume();
                        }
                    }
                    case S -> {
                        if (e.isControlDown()) {
                            onSessionStartup(null);
                            e.consume();
                        }
                    }
                    case UP -> {
                        onNavPrev(null);
                        e.consume();
                    }
                    case DOWN -> {
                        onNavNext(null);
                        e.consume();
                    }
                    case F2 -> {
                        darkMode.setSelected(!darkMode.isSelected());
                        ThemeManager.toggle(scene, darkMode.isSelected());;
                    }
                    case PLUS, ADD -> {
                        if (e.isControlDown()) {
                            onZoomIn(null);
                            e.consume();
                        }
                    }
                    case MINUS, SUBTRACT -> {
                        if (e.isControlDown()) {
                            onZoomOut(null);
                            e.consume();
                        }
                    }
                }
            });
        });
    }

    // Session Startup popup
    @FXML
    private void onSessionStartup(ActionEvent e) {
        initializeProfileComboBox();
        sessionPopupOverlay.setVisible(true);
        sessionPopupOverlay.setDisable(false);
        workspaceView.setDisable(true);
    }

    @FXML private void onSessionPopupClose(ActionEvent e) {
        sessionPopupOverlay.setVisible(false);
        sessionPopupOverlay.setDisable(true);
        workspaceView.setDisable(false);
    }

    @FXML
    private void onStartSession(ActionEvent e) {
        Profile profile = comboBoxProfiles.getValue();
        String boxInput = txtFldBoxId.getText().trim();

        if (profile == null) {
            AlertHelper.showError("Session Setup", "Please select a profile before starting.");
            // TODO add visual error feedback
            return;
        }
        if (boxInput.isEmpty()) {
            AlertHelper.showError("Session Setup", "Please enter a Box ID before starting.");
            // TODO add visual error feedback
            return;
        }

        try {
            Box activeBox = modelFacade.getBoxModel().getOrCreateSessionBox(boxInput, profile);
            root.setValue(activeBox);
            scanModel = new ScanModel(activeBox);
            User currentUser = modelFacade.getSessionModel().getCurrentUser();
            modelFacade.getLogModel().createLog(new Log(currentUser, scanModel.getTargetBox().getBoxId(), EntityType.BOX, LogAction.CREATE, LocalDateTime.now()));
            syncDocumentsFromModel();
            initializeTreeView(boxTreeView, activeBox);

            sessionActive = true;
            selectedDocument = null;
            selectedFile = null;
            selectedBox = null;

            setSessionControlsDisabled(false);
            lblSessionStatus.setText("Profile: " + profile.getProfileName() + "   Box: " + activeBox.getBoxName());
            sessionPopupOverlay.setVisible(false);
            sessionPopupOverlay.setDisable(true);
            workspaceView.setDisable(false);
            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Session Setup", "Could not start scan session.");
        }
    }

    /**
     * Starts the continuous scan loop on a background thread.
     * <p>
     * A {@value scanDelay} ms delay is inserted after each successful scan
     * so the scanner hardware has time to advance the next page.
     * The first scan of an empty box always fetches a barcode page (enforced by ScanManager).
     */
    @FXML
    private void onScan(ActionEvent e) {
        if (!sessionActive || scanModel == null || scanning) return;

        scanning = true;
        btnScan.setDisable(true);

        scanThread = new Thread(() -> {
            while (scanning) {
                try {
                    ScanManager.StoredScan result = scanModel.fetchScan();

                    if (!scanning) break; // stop was pressed during fetch (leave loop)

                    Platform.runLater(() -> {
                        syncDocumentsFromModel();
                        selectPage(result.document(), result.file());
                        rebuild();
                    });

                    Thread.sleep(scanDelay);

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break; // scanning already false
                } catch (Exception ex) {
                    scanning = false;
                    Platform.runLater(() -> {
                        btnScan.setDisable(false);
                        btnStop.setDisable(true);
                        AlertHelper.showError("Scan Failed",
                                "Scanning stopped. Could not fetch the next page. Please try again.");
                    });
                    return;
                }
            }

            // loop condition became false (stop was pressed and fetch completed)
            Platform.runLater(() -> {
                btnScan.setDisable(false);
                btnStop.setDisable(true);
            });
        });

        scanThread.setDaemon(true);
        scanThread.start();
    }

    /** Stops automatic scanning after the current fetch completes. */
    @FXML
    private void onStop(ActionEvent e) {
        if (!scanning || !sessionActive || scanModel == null) return;

        scanning = false;
        btnStop.setDisable(true);

        if (scanThread != null) {
            scanThread.interrupt(); // wake sleep immediately
        }
    }

    @FXML
    private void onNewDocument(ActionEvent e) {
        if (!sessionActive || scanModel == null) return;

        try {
            Document newDoc = scanModel.manualSplit();
            syncDocumentsFromModel();

            // select the new document so the tree highlights it;
            selectedDocument = newDoc;
            selectedFile = null;

            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("New Document Failed", "Could not create a new document. Please try again.");
        }
    }

    /**
     * TODO: prompt a before or after split while selecting a file
     */
    @FXML
    private void onSplitDocument(ActionEvent actionEvent) {
        // TODO: implement split
    }

    @FXML
    private void onDeleteFileOrDocument(ActionEvent e) {
        if (scanModel == null || !sessionActive) return;

        try {
            if (selectedFile != null) {
                int deletedIndex = currentPageIndex();
                scanModel.deleteFile(selectedFile);

                List<Document> emptyDocuments = new ArrayList<>(documents);
                emptyDocuments.removeIf(document -> !document.getFiles().isEmpty());
                for (Document emptyDocument : emptyDocuments) {
                    scanModel.deleteDocument(emptyDocument);
                }

                syncDocumentsFromModel(); // resync the observable list so that it matches the models

                // clear selection if nothing left, else select nearest file
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
                syncDocumentsFromModel();
                selectedDocument = null;
                selectedFile = null;
            } else if (selectedBox != null) {
                System.out.println("DELETING BOX");
                onDeleteBox();
            }else return;

            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Delete Failed", "Could not delete the selected "
                    + (selectedFile != null ? "file." : "document. ") + "Please try again.");
        }
    }

    @FXML private void onRotateLeft(ActionEvent e)  { rotatePage(-1); }
    @FXML private void onRotateRight(ActionEvent e) { rotatePage(1); }
    @FXML
    private void onFileAdjustments(ActionEvent actionEvent) {
        if (selectedFile == null || scanModel == null) return;
        populateFileAdjustmentsFields(selectedFile);
        sessionPopupOverlay1.setVisible(true);
        sessionPopupOverlay1.setDisable(false);
        workspaceView.setDisable(true);
    }

    @FXML
    private void onFileAdjustmentsClose(ActionEvent e) {
        sessionPopupOverlay1.setVisible(false);
        sessionPopupOverlay1.setDisable(true);
        workspaceView.setDisable(false);
    }

    @FXML
    private void onApplyFileAdjustments(ActionEvent e) {
        if (selectedFile == null || scanModel == null) return;
        try {
            int rotation = parseRotationField(txtFldIndividualFileRotation);
            double hue = parseDoubleField(txtFldIndividualFileHue,"Hue",-1.0, 1.0);
            double brightness = parseDoubleField(txtFldIndividualFileBrightness,"Brightness",-1.0,1.0);
            double contrast = parseDoubleField(txtFldIndividualFileContrast,"Contrast",-1.0,1.0);
            double saturation = parseDoubleField(txtFldIndividualFileSaturation,"Saturation",-1.0,1.0);

            FileAdjustmentSettings settings = new FileAdjustmentSettings(rotation, hue, brightness, contrast, saturation);
            scanModel.updateFileSettings(selectedFile, settings);
            rebuildPreviewCard();
            onFileAdjustmentsClose(null);
        } catch (IllegalArgumentException ex) {
            AlertHelper.showError("Invalid Input", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Settings Failed", "Could not apply file settings. Please try again.");
        }
    }

    private void onDeleteBox() {

        AlertHelper.showConfirmation("Delete Box", "Are you sure you want to delete this box?\n"
        + "This action is permanent and cannot be undone.", () -> {
            try {
                scanModel.deleteBox();
                modelFacade.getBoxModel().deleteBox(scanModel.getTargetBox());
                User currentUser = modelFacade.getSessionModel().getCurrentUser();
                modelFacade.getLogModel().createLog(new Log(currentUser, scanModel.getTargetBox().getBoxId(), EntityType.BOX, LogAction.DELETE, LocalDateTime.now()));
                boxTreeView.setShowRoot(false);

                sessionActive = false;
                scanModel = null;
                documents.clear();
                selectedBox = null;
                selectedDocument = null;
                selectedFile = null;

                setSessionControlsDisabled(true);
                sessionPopupOverlay.setVisible(true);
                sessionPopupOverlay.setDisable(false);
                workspaceView.setDisable(true);
                lblSessionStatus.setText("");

                rebuild();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    private int parseRotationField(TextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rotation must be a whole number (0, 90, 180 or 270).");
        }
    }

    private double parseDoubleField(TextField field, String name, double min, double max) {
        double value;
        try {
            value = Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a number between " + min + " and " + max + ".");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    private void rotatePage(int direction) {
        if (selectedFile == null || scanModel == null) return;

        int degrees = spinnerRotation.getValue() * direction;
        int rotation = normaliseRotation(selectedFile.getRotation() + degrees);
        try {
            scanModel.updateFileRotation(selectedFile, rotation);
            rebuildPreviewCard();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Rotation Failed", "Could not update file rotation.");
        }
    }

    // TreeView navigation
    @FXML private void onNavFirst(ActionEvent e) { navigateTo(0); }
    @FXML private void onNavPrev(ActionEvent e)  { navigateTo(currentPageIndex() - 1); }
    @FXML private void onNavNext(ActionEvent e)  { navigateTo(currentPageIndex() + 1); }
    @FXML private void onNavLast(ActionEvent e)  { navigateTo(allPages().size() - 1); }

    private boolean navigating = false;

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

        // Detach listener, sync tree, then reattach — prevents selection event snapping back
        boxTreeView.getSelectionModel().selectedItemProperty().removeListener(treeSelectionListener);
        rebuildPreviewCard();
        syncTreeSelection();
        boxTreeView.getSelectionModel().selectedItemProperty().addListener(treeSelectionListener);
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
     * Saves all staged data to the database, then exports every document to a
     * user-chosen directory on the local filesystem.
     */
    @FXML
    private void onExport(ActionEvent e) {
        if (!sessionActive || scanModel == null || scanning) return;

        if (documents.isEmpty() || totalPageCount() == 0) {
            AlertHelper.showError("Export", "There are no documents to export.");
            return;
        }

        ExportMode mode = comboBoxExport.getValue();
        if (mode == null) {
            AlertHelper.showError("Export", "Please select an export mode.");
            return;
        }

        // ask user where to save the export
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Export Destination");
        java.io.File exportDirectory = chooser.showDialog(currentStage);
        if (exportDirectory == null) return; // user cancelled

        try {
            scanModel.save(); // persist staged data first
            scanModel.export(exportDirectory, mode);
            User currentUser = modelFacade.getSessionModel().getCurrentUser();
            modelFacade.getLogModel().createLog(new Log(currentUser, scanModel.getTargetBox().getBoxId(), EntityType.BOX, LogAction.EXPORT, LocalDateTime.now()));
            rebuild();
            AlertHelper.showInformation("Export Complete", "Export finished. \nFiles saved to:" + exportDirectory.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Export Failed", "Could not export documents. Please try again.");
        }
    }

    @FXML
    private void onUndo(ActionEvent e) {
        // TODO: implement undo
    }

    @FXML
    private void onDarkModeToggle() { ThemeManager.toggle(currentStage.getScene(), darkMode.isSelected()); }

    @FXML
    private void onExit(ActionEvent actionEvent) {
        AlertHelper.showConfirmation("Exit Window", "Are you sure you want to exit?\n"
                + "Any unsaved progress will be deleted.", () -> {
            try {
                User user = modelFacade.getSessionModel().getCurrentUser();
                ViewHandler handler = user.isAdmin() ? ViewHandler.ADMIN : ViewHandler.LOGIN;
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

    private void onTreeSelectionChanged(TreeItem<TreeNode> item) {
        if (navigating) return;
        if (item == null || item.getValue() == null) return;

        TreeNode value = item.getValue();
        System.out.println(value.getClass());
        if (value instanceof Document document) {
            System.out.println("SELECTED DOCUMENT");
            selectedDocument = document;
            selectedFile = null;
            selectedBox = null;
        } else if (value instanceof File file) {
            System.out.println("SELECTED FILE");
            for (Document document : documents) {
                if (document.getFiles().contains(file)) {
                    selectPage(document, file);
                    break;
                }
            }
        } else if (value instanceof Box box) {
            System.out.println("SELECTED BOX");
            selectedBox = box;
            selectedDocument = null;
            selectedFile = null;

        }
        else {
            System.out.println(value.getClass());
        }

        rebuildPreviewCard();
    }

    private boolean moveFileToDocument(File file, Document target) {
        Document source = findOwnerDocument(file);
        if (source == null || source == target) return false;

        source.getFiles().remove(file);
        target.getFiles().add(file);
        persistFileMoved(file, target);
        return true;
    }

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

    private boolean reorderDocument(Document dragged, Document target) {
        if (dragged == target) return false;

        documents.remove(dragged);
        int insertIndex = documents.indexOf(target);
        documents.add(insertIndex, dragged);
        return true;
    }

    private void persistFileMoved(File file, Document newOwner) {
        try {
            modelFacade.getFileModel().moveFile(file, newOwner.getDocumentId());
        } catch (Exception e) {
            AlertHelper.showError("Move Failed", "Could not persist file move: " + e.getMessage());
        }
    }

    private void rebuild() {
        rebuildPreviewCard();
        refreshTree();
        refreshStatusBar();
    }

    /**
     * Rebuilds the center preview card.
     * <p>
     * When a document is selected but no specific file is chosen (e.g. right after
     * a barcode split or manual split where the new doc now has a file), the first
     * file of that document is shown automatically.
     */
    private void rebuildPreviewCard() {
        pageGrid.getChildren().clear();

        if (selectedDocument != null && selectedFile != null) {
            pageGrid.getChildren().add(buildPageCard(selectedDocument, selectedFile));
        } else if (selectedDocument != null && !selectedDocument.getFiles().isEmpty()) {
            pageGrid.getChildren().add(buildPageCard(selectedDocument, selectedDocument.getFiles().getFirst()));
        }

        updatePageInfoLabel();
        lblEmptyState.setVisible(pageGrid.getChildren().isEmpty());
    }

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

        // Sync tree selection with selectedFile / selectedDocument
        syncTreeSelection();
    }

    /**
     * Walks the rebuilt tree to find the TreeItem that matches the currently
     * selected file (or document if no file is selected) and highlights it.
     */
    private void syncTreeSelection() {
        TreeItem<TreeNode> root = boxTreeView.getRoot();

        for (TreeItem<TreeNode> docItem : root.getChildren()) {
            Document doc = (Document) docItem.getValue();

            if (selectedFile != null) {
                for (TreeItem<TreeNode> fileItem : docItem.getChildren()) {
                    if (fileItem.getValue() == selectedFile) {
                        boxTreeView.getSelectionModel().select(fileItem);
                        boxTreeView.scrollTo(boxTreeView.getSelectionModel().getSelectedIndex());
                        return;
                    }
                }
            } else if (selectedDocument != null && doc == selectedDocument) {
                boxTreeView.getSelectionModel().select(docItem);
                boxTreeView.scrollTo(boxTreeView.getSelectionModel().getSelectedIndex());
                return;
            }
        }
    }

    private void refreshStatusBar() {
        stDocsLabel.setText("Documents: " + documents.size());
        stPagesLabel.setText("Files: " + totalPageCount());
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
        thumb.setEffect(new ColorAdjust(file.getHue() / 100, file.getSaturation() / 100, file.getBrightness() / 100, file.getContrast() / 100));

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

    // ---------- HELPERS ----------

    private void updateProfileAdjustmentsFields(Profile profile) {
        if (profile == null || profile.getFileAdjustmentSettings() == null) {
            txtFldGlobalRotation.clear();
            txtFldGlobalHue.clear();
            txtFldGlobalBrightness.clear();
            txtFldGlobalContrast.clear();
            txtFldGlobalSaturation.clear();
            return;
        }
        FileAdjustmentSettings settings = profile.getFileAdjustmentSettings();
        txtFldGlobalRotation.setText(String.valueOf(settings.getRotation()));
        txtFldGlobalHue.setText(String.valueOf(settings.getHue()));
        txtFldGlobalBrightness.setText(String.valueOf(settings.getBrightness()));
        txtFldGlobalContrast.setText(String.valueOf(settings.getContrast()));
        txtFldGlobalSaturation.setText(String.valueOf(settings.getSaturation()));
    }

    private void populateFileAdjustmentsFields(File file) {
        txtFldIndividualFileRotation.setText(String.valueOf(file.getRotation()));
        txtFldIndividualFileHue.setText(String.valueOf(file.getHue()));
        txtFldIndividualFileBrightness.setText(String.valueOf(file.getBrightness()));
        txtFldIndividualFileContrast.setText(String.valueOf(file.getContrast()));
        txtFldIndividualFileSaturation.setText(String.valueOf(file.getSaturation()));
    }

    private void selectPage(Document document, File file) {
        selectedDocument = document;
        selectedFile = file;
    }

    /** Syncs the observable list from the model's in-memory box state. */
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
        btnSplitDoc.setDisable(disabled);
        btnDelete.setDisable(disabled);
        btnUndo.setDisable(disabled);
        btnExport.setDisable(disabled);
        btnZoomOut.setDisable(disabled);
        btnZoomIn.setDisable(disabled);
    }

    private String documentLabel(Document document) {
        int position = documents.indexOf(document) + 1;
        return "Document " + position;
    }

    private String fileLabel(File file) {
        for (Document doc : documents) {
            int position = doc.getFiles().indexOf(file);
            if (position >= 0) {
                return "Page " + (position + 1);
            }
        }
        return "Page ?"; // should not happen - something's wrong
    }

    private int currentPageIndex() {
        if (selectedFile == null) return -1;
        return allPages().indexOf(selectedFile);
    }

    private Document findOwnerDocument(File file) {
        return documents.stream().filter(document -> document.getFiles().contains(file)).findFirst().orElse(null);
    }

    private int normaliseRotation(int rotation) {
        return ((rotation % 360) + 360) % 360;
    }
}
