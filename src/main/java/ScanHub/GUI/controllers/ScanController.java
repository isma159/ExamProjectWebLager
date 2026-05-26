package ScanHub.GUI.controllers;

import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.ExportMode;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.interfaces.TreeNode;
import ScanHub.GUI.util.ThemeHandler;
import ScanHub.GUI.util.GlobalKeyHandler;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import ScanHub.GUI.models.ScanModel;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.ToggleSwitch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class ScanController implements Initializable, IViewController {

    @FXML private BorderPane workspaceView;
    @FXML private Label lblUsername, lblRole, lblEmptyState;
    @FXML private ToggleSwitch darkMode;
    @FXML private Button btnScan, btnStop, btnRotLeft, btnRotRight, btnUndo, btnExport, btnZoomOut, btnZoomIn, btnFileAdjustments;
    @FXML private ComboBox<ExportMode> comboBoxExport;
    @FXML private FlowPane pageGrid;
    @FXML private Label lblSessionStatus, lblCurrentPage, lblCurrentDocument, lblTotalDocuments, lblTotalPages;
    @FXML private TreeView<TreeNode> boxTreeView;
    @FXML private Spinner<Integer> spinnerRotation;
    @FXML private ProgressBar progressBarExport;
    @FXML private HBox hboxProgressBarExport;

    // Session Startup Popup
    @FXML private StackPane sessionPopupOverlay;
    @FXML private SearchableComboBox<Profile> comboBoxProfiles;
    @FXML private TextField txtFldBoxId, txtFldGlobalRotation, txtFldGlobalHue, txtFldGlobalBrightness, txtFldGlobalContrast, txtFldGlobalSaturation;

    // File Adjustment Menu
    @FXML private StackPane fileAdjustmentSideMenu;
    @FXML private Spinner<Integer> spinnerFileAdjustmentRotation, spinnerFileAdjustmentHue, spinnerFileAdjustmentBrightness,
            spinnerFileAdjustmentContrast, spinnerFileAdjustmentSaturation, spinnerFileAdjustmentSharpness;
    @FXML private Slider sliderHue, sliderBrightness, sliderContrast, sliderSaturation, sliderSharpness;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private ScanModel scanModel;
    private User currentUser = null;
    private final ObservableList<Document> documents = FXCollections.observableArrayList();
    private Document selectedDocument;
    private File selectedFile;
    private Box selectedBox;
    private ImageView currentPreviewImageView;
    private boolean sessionActive;

    private final TreeItem<TreeNode> root = new TreeItem<>();
    private final ChangeListener<TreeItem<TreeNode>> treeSelectionListener =
            (obs, oldValue, newValue) -> onTreeSelectionChanged(newValue);
    private TreeNode draggedNode; // used for drag detection (gets nulled after drop)

    private final Deque<Runnable> undoStack = new ArrayDeque<>();
    private static final int maxUndos = 30;

    private Thread scanThread; // scan loop is controlled by the volatile boolean 'scanning', not thread interruption
    private volatile boolean scanning = false; // volatile: FX-thread writes are immediately visible to the scan thread
    private static final long scanDelay = 1000; // milliseconds to wait between successive scans in the scan loop

    // Zoom Level stuff
    private double zoomLevel = 1.0; // default
    private static final double ZOOM_STEP = 0.15;
    private static final double ZOOM_MIN  = 0.40;
    private static final double ZOOM_MAX  = 3.00;

    @Override
    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;

        currentUser = modelFacade.getSessionModel().getCurrentUser();
        lblUsername.setText(currentUser.getUsername());
        lblRole.setText("Role: " + currentUser.getRole().toString());

        initializeProfileComboBox();

        currentStage.setOnCloseRequest(event -> {
            endScanSession();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeKeyboardShortcuts();
        initializeExportComboBoxes();
        setFileAdjustmentSideMenu(false);
        setFileAdjustmentBtn(false);
        setExportInProgress(false);

        comboBoxProfiles.valueProperty().addListener((obs, oldValue, newValue) -> updateProfileAdjustmentsFields(newValue));
        spinnerRotation.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 360, 5, 1));

        spinnerFileAdjustmentRotation.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-360, 360, 0, 1));
        spinnerFileAdjustmentHue.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spinnerFileAdjustmentBrightness.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spinnerFileAdjustmentContrast.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spinnerFileAdjustmentSaturation.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spinnerFileAdjustmentSharpness.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));

        bindSlider(sliderHue, spinnerFileAdjustmentHue);
        bindSlider(sliderBrightness, spinnerFileAdjustmentBrightness);
        bindSlider(sliderContrast, spinnerFileAdjustmentContrast);
        bindSlider(sliderSaturation, spinnerFileAdjustmentSaturation);
        bindSlider(sliderSharpness, spinnerFileAdjustmentSharpness);

        sliderSharpness.setOnMouseReleased(e -> applySharpnessToPreview((float) spinnerFileAdjustmentSharpness.getValue() / 100f));

        setSessionControlsDisabled(true);
        refreshStatusBar();
        updateCurrentPageLabel();
        updateCurrentDocumentLabel();
    }

    private void initializeProfileComboBox() {
        if (modelFacade == null) return;

        if (currentUser.getProfiles().isEmpty() && !currentUser.isAdmin()) return;

        if (currentUser.isAdmin()) {
            currentUser.setProfiles(modelFacade.getProfileModel().getProfiles());
        }

        comboBoxProfiles.setItems(FXCollections.observableArrayList(currentUser.getProfiles()));
    }

    private void initializeExportComboBoxes() {
        comboBoxExport.setItems(FXCollections.observableArrayList(ExportMode.values()));
        comboBoxExport.getSelectionModel().selectFirst();
    }

    /**
     * Initializes the left-panel tree view with icons for Box, Documents, and Files,
     * drag-and-drop functions, reordering, and auto-expand on structural changes.
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
                    setContextMenu(null);
                    return;
                }

                Label icon = new Label();
                icon.getStyleClass().add("icon");

                if (object instanceof Box box) {
                    icon.setText("\ue9d9");
                    setText(box.getBoxName());
                    setStyle(box.isStaged() || box.isModified() ? "-fx-font-weight: bold;" : "");

                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem deleteBox = menuItemSetup("Delete Box", "Delete", () -> onDeleteFileOrDocument(null));
                    MenuItem newDoc = menuItemSetup("New Document", "Ctrl + N", () -> onNewDocument());

                    contextMenu.getItems().addAll(deleteBox, newDoc);
                    setContextMenu(contextMenu);
                } else if (object instanceof Document document) {
                    icon.setText("\ue963");
                    setText(setDocumentLabel(document));
                    setStyle(document.isStaged() || document.isModified() ? "-fx-font-weight: bold;" : "");

                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem deleteDoc = menuItemSetup("Delete Document", "Delete", () -> onDeleteFileOrDocument(null));

                    contextMenu.getItems().add(deleteDoc);
                    setContextMenu(contextMenu);
                } else if (object instanceof File file) {
                    icon.setText("\ue958");
                    setText(setFileLabel(file));
                    setStyle(file.isStaged() || file.isModified() ? "-fx-font-weight: bold;" : "");

                    // context menu :)
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem splitBefore = menuItemSetup("Split Before", "Ctrl + A", () -> onSplitDocument(0));
                    MenuItem splitAfter = menuItemSetup("Split After", "Ctrl + D", () -> onSplitDocument(1));

                    MenuItem deleteFile = menuItemSetup("Delete File", "Delete", () -> onDeleteFileOrDocument(null));

                    contextMenu.getItems().addAll(splitBefore, splitAfter, deleteFile);
                    setContextMenu(contextMenu);
                }

                setGraphic(icon);
            }
        });
    }

    private MenuItem menuItemSetup(String command, String shortcut, Runnable onSelected) {

        MenuItem menuItem = new MenuItem();

        Label commandLbl = new Label(command);
        commandLbl.setMinWidth(120);
        commandLbl.setMaxWidth(120);
        commandLbl.setAlignment(Pos.CENTER_LEFT);

        Region growingSpacer = new Region();
        growingSpacer.setMinWidth(30);
        HBox.setHgrow(growingSpacer, Priority.ALWAYS);

        Label shortcutLbl = new Label(shortcut);
        shortcutLbl.setMinWidth(45);
        shortcutLbl.setAlignment(Pos.CENTER_RIGHT);

        HBox container = new HBox(commandLbl, growingSpacer, shortcutLbl);

        menuItem.setGraphic(container);

        menuItem.setOnAction(e -> {
            onSelected.run();
            e.consume();
        });

        return menuItem;

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
        // Build the shortcut map for the Scan workspace
        Map<KeyCodeCombination, Runnable> shortcuts = new HashMap<>();

        shortcuts.put(new KeyCodeCombination(KeyCode.SPACE),
                () -> { if (!scanning) onScan(null); else onStop(null); });
        shortcuts.put(new KeyCodeCombination(KeyCode.LEFT,
                        KeyCombination.CONTROL_DOWN),
                () -> onRotateLeft(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.RIGHT,
                        KeyCombination.CONTROL_DOWN),
                () -> onRotateRight(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.UP),
                () -> onNavPrev(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.DOWN),
                () -> onNavNext(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.PAGE_UP),
                () -> onNavFirst(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.PAGE_DOWN),
                () -> onNavLast(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.DELETE),
                () -> onDeleteFileOrDocument(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.N,
                        KeyCombination.CONTROL_DOWN),
                 this::onNewDocument);
        shortcuts.put(new KeyCodeCombination(KeyCode.A,
                KeyCombination.CONTROL_DOWN),
                () -> onSplitDocument(0));
        shortcuts.put(new KeyCodeCombination(KeyCode.D,
                        KeyCombination.CONTROL_DOWN),
                () -> onSplitDocument(1));
        shortcuts.put(new KeyCodeCombination(KeyCode.E,
                        KeyCombination.CONTROL_DOWN),
                () -> onExport(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.S,
                        KeyCombination.CONTROL_DOWN),
                () -> onSessionStartup(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.F2),
                () -> { darkMode.setSelected(!darkMode.isSelected());
                    ThemeHandler.toggle(pageGrid.getScene(), darkMode.isSelected()); });
        shortcuts.put(new KeyCodeCombination(KeyCode.PLUS,
                        KeyCombination.CONTROL_DOWN),
                () -> onZoomIn(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.ADD,
                        KeyCombination.CONTROL_DOWN),
                () -> onZoomIn(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.MINUS,
                        KeyCombination.CONTROL_DOWN),
                () -> onZoomOut(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.SUBTRACT,
                        KeyCombination.CONTROL_DOWN),
                () -> onZoomOut(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.Z,
                        KeyCombination.CONTROL_DOWN),
                () -> onUndo(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.ESCAPE),
                () -> onExit(null));
        shortcuts.put(new KeyCodeCombination(KeyCode.F,
                        KeyCombination.CONTROL_DOWN),
                () -> onToggleFileAdjustments(null));

        GlobalKeyHandler.getInstance().setLayer(shortcuts);
    }

    @FXML
    private void onSessionStartup(ActionEvent e) {
        initializeProfileComboBox();
        sessionPopupOverlay.setVisible(true);
        sessionPopupOverlay.setDisable(false);
        workspaceView.setDisable(true);
    }

    @FXML
    private void onSessionPopupClose(ActionEvent e) {
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
            if (scanModel != null) {
                scanModel.getTargetBox().getDocuments().clear();
                endScanSession();
            }

            Box activeBox = modelFacade.getBoxModel().getOrCreateSessionBox(boxInput, profile);

            modelFacade.getSessionModel().cleanup();

            boolean locked = modelFacade.getSessionModel().tryStartScanSession(activeBox.getBoxName());
            if (!locked) {
                AlertHelper.showError("Box In Use", "This box is currently open in another session.");
                return;
            }

            root.setValue(activeBox);
            scanModel = new ScanModel(activeBox);
            // todo modelFacade.getLogModel().createLog(new Log(currentUser, Integer.parseInt(scanModel.getTargetBox().getBoxName()), EntityType.BOX, LogAction.CREATE, LocalDateTime.now()));
            syncDocumentsFromModel();
            initializeTreeView(boxTreeView, activeBox);

            sessionActive = true;
            selectedBox = activeBox;

            setSessionControlsDisabled(false);
            lblSessionStatus.setText(""); // TODO display something or nah?
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
     * so the scanner has time to advance the next page.
     * The first scan of an empty box always fetches a barcode page (enforced by ScanManager).
     * <p>
     * Each scan result pushes one undo entry. The undo removes the scanned file.
     * If a new document was created by a barcode split undo removes that document too.
     */
    @FXML
    private void onScan(ActionEvent e) {
        if (!sessionActive || scanModel == null || scanning) return;

        scanning = true;
        btnScan.setDisable(true);
        btnStop.setDisable(false);

        scanThread = new Thread(() -> {
            while (scanning) {
                try {
                    StoredScan result = scanModel.fetchScan();

                    if (!scanning) break; // stop was pressed during fetch (leave loop)

                    Platform.runLater(() -> {
                        // snapshot document list before sync to detect newly created documents
                        Set<Document> documentsBefore = new HashSet<>(documents);

                        syncDocumentsFromModel();
                        selectPage(result.document(), result.file());
                        rebuild();

                        // determine whether fetchScan created a brand-new document
                        Document scannedDocument = result.document();
                        File scannedFile = result.file();
                        boolean newDocumentCreated = !documentsBefore.contains(scannedDocument);

                        pushUndo(() -> {
                            scannedDocument.getFiles().remove(scannedFile);

                            // if a new document was created by a barcode split, remove it too
                            if (newDocumentCreated) {
                                try {
                                    scanModel.deleteDocument(scannedDocument);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }

                            syncDocumentsFromModel();

                            // move selection to the last remaining page or clear it
                            List<File> remaining = allPages();
                            if (remaining.isEmpty()) {
                                selectedDocument = null;
                                selectedFile = null;
                            } else {
                                selectedFile = remaining.getLast();
                                selectedDocument = findOwnerDocument(selectedFile);
                            }
                        });
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
                        AlertHelper.showError("Scan Failed", "Scanning stopped. Could not fetch the next page. Please try again.");
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

    /** Creates a new empty document and selects it. */
    private void onNewDocument() {
        if (!sessionActive || scanModel == null) return;

        try {
            Document newDocument = scanModel.manualSplit();
            syncDocumentsFromModel();

            // select the new document so the tree highlights it
            selectedDocument = newDocument;
            selectedFile = null;

            pushUndo(() -> {
                try {
                    scanModel.deleteDocument(newDocument);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                syncDocumentsFromModel();
                selectedDocument = null;
                selectedFile = null;
            });

            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("New Document Failed", "Could not create a new document. Please try again.");
        }
    }

    private void endScanSession() {
        System.out.println("endScanSession called - sessionActive: " + sessionActive + " scanModel: " + scanModel);
        if (!sessionActive || scanModel == null) {return;}

        try {
            System.out.println("deleting session for: " + scanModel.getTargetBox().getBoxName());
            modelFacade.getSessionModel().endScanSession(scanModel.getTargetBox().getBoxName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        sessionActive = false;
        scanModel = null;
        documents.clear();
        selectedBox = null;
        selectedDocument = null;
        selectedFile = null;
        undoStack.clear();
    }

    private void onSplitDocument(int choice) {
        if (selectedFile == null || selectedDocument == null || scanModel == null || !sessionActive) return;

        List<File> files = selectedDocument.getFiles();
        int splitIndex = files.indexOf(selectedFile);
        if (splitIndex <= 0) return; // nothing to split if it's the first page
        int actualSplitIndex = choice == 0 ? splitIndex : splitIndex + 1;

        try {
            // collect all files from the split point onwards
            List<File> toMove = new ArrayList<>(files.subList(actualSplitIndex, files.size()));
            Document originalDocument = selectedDocument;

            Document newDocument = scanModel.manualSplit();

            // manualSplit() appends the new document to the end of the box.
            // For a split we want it to appear directly after the document it
            // was split from, so reposition it in the model before syncing.
            List<Document> modelDocuments = scanModel.getTargetBox().getDocuments();
            modelDocuments.remove(newDocument);
            int originalIndex = modelDocuments.indexOf(originalDocument);
            int insertIndex = originalIndex < 0 ? modelDocuments.size() : originalIndex + 1;
            modelDocuments.add(insertIndex, newDocument);

            syncDocumentsFromModel();
            // move files into the new document
            for (File file : toMove) {
                originalDocument.getFiles().remove(file);
                newDocument.getFiles().add(file);
            }

            selectedDocument = newDocument;
            selectedFile = toMove.getFirst();

            pushUndo(() -> {
                // return every moved file to the original document in order
                for (File file : toMove) {
                    newDocument.getFiles().remove(file);
                    originalDocument.getFiles().add(file);
                }
                try {
                    scanModel.deleteDocument(newDocument);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                syncDocumentsFromModel();
                selectedDocument = originalDocument;
                selectedFile = toMove.getFirst();
            });

            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Split Failed", "Could not split the document. Please try again.");
        }
    }

    /**
     * Deletes the currently selected file, document, or box.
     * <p>
     * File deletion also deletes any documents that are left empty afterward.
     * <p>
     * Undo for a file re-inserts it at its original index in its owner document,
     * and re-inserts any documents that were auto-removed because they became empty.
     * Undo for a document re-inserts the entire document (with its files) at its
     * original position in the list.
     */
    @FXML
    private void onDeleteFileOrDocument(ActionEvent e) {
        if (scanModel == null || !sessionActive) return;

        try {
            if (selectedFile != null) {
                Document ownerDoc = findOwnerDocument(selectedFile);
                if (ownerDoc == null) return;

                // capture position before any changes
                final int fileIndex  = ownerDoc.getFiles().indexOf(selectedFile);
                final File capturedFile = selectedFile;
                final Document capturedOwnerDoc = ownerDoc;

                // snapshot every document that will be auto-removed after the file deletion
                final List<AbstractMap.SimpleEntry<Integer, Document>> docsToRestore = new ArrayList<>();
                for (int i = 0; i < documents.size(); i++) {
                    Document doc = documents.get(i);
                    if (doc.getFiles().isEmpty() || (doc == ownerDoc && doc.getFiles().size() == 1)) {
                        docsToRestore.add(new AbstractMap.SimpleEntry<>(i, doc));
                    }
                }

                int deletedPageIndex = currentPageIndex();
                scanModel.deleteFile(capturedFile);

                List<Document> emptyDocuments = new ArrayList<>(documents);
                emptyDocuments.removeIf(doc -> !doc.getFiles().isEmpty());
                for (Document emptyDoc : emptyDocuments) {
                    scanModel.deleteDocument(emptyDoc);
                }

                syncDocumentsFromModel();

                List<File> remaining = allPages();
                if (remaining.isEmpty()) {
                    selectedDocument = null;
                    selectedFile = null;
                } else {
                    int next = Math.min(deletedPageIndex, remaining.size() - 1);
                    selectedFile = remaining.get(Math.max(next, 0));
                    selectedDocument = findOwnerDocument(selectedFile);
                }

                pushUndo(() -> {
                    List<Document> modelDocs = scanModel.getTargetBox().getDocuments();

                    // re-insert auto-removed documents at their original positions
                    docsToRestore.sort(Comparator.comparingInt(AbstractMap.SimpleEntry::getKey));
                    for (AbstractMap.SimpleEntry<Integer, Document> entry : docsToRestore) {
                        if (!modelDocs.contains(entry.getValue())) {
                            int idx = Math.min(entry.getKey(), modelDocs.size());
                            modelDocs.add(idx, entry.getValue());
                        }
                    }

                    // re-insert the deleted file into its owner document
                    if (!capturedOwnerDoc.getFiles().contains(capturedFile)) {
                        int insertIdx = Math.min(fileIndex, capturedOwnerDoc.getFiles().size());
                        capturedOwnerDoc.getFiles().add(insertIdx, capturedFile);
                    }

                    syncDocumentsFromModel();
                    selectPage(capturedOwnerDoc, capturedFile);
                });

            } else if (selectedDocument != null) {
                final int documentIndex = documents.indexOf(selectedDocument);
                final Document capturedDoc = selectedDocument;
                // snapshot the file list so undo can restore the full document contents
                final List<File> capturedFiles = new ArrayList<>(selectedDocument.getFiles());

                scanModel.deleteDocument(capturedDoc);
                syncDocumentsFromModel();
                selectedDocument = null;
                selectedFile = null;

                pushUndo(() -> {
                    List<Document> modelDocs = scanModel.getTargetBox().getDocuments();
                    if (!modelDocs.contains(capturedDoc)) {
                        // restore files onto the document object before re-inserting
                        capturedDoc.getFiles().clear();
                        capturedDoc.getFiles().addAll(capturedFiles);
                        int idx = Math.min(documentIndex, modelDocs.size());
                        modelDocs.add(idx, capturedDoc);
                    }
                    syncDocumentsFromModel();
                    selectedDocument = capturedDoc;
                    selectedFile = capturedFiles.isEmpty() ? null : capturedFiles.getFirst();
                });

            } else if (selectedBox != null) {
                onDeleteBox();
            }else return;

            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            String itemType = selectedFile != null ? "selected file" : selectedDocument != null ? "selected document" : "box";
            AlertHelper.showError("Delete Failed", "Could not delete the "
                    + itemType + ". Please try again.");
        }
    }

    @FXML private void onRotateLeft(ActionEvent e) { rotatePage(-1); }
    @FXML private void onRotateRight(ActionEvent e) { rotatePage(1); }

    @FXML
    private void onToggleFileAdjustments(ActionEvent actionEvent) {
        if (selectedFile == null) return;
        if (!fileAdjustmentSideMenu.isVisible()) {
            populateFileAdjustmentsFields(selectedFile);
            setFileAdjustmentSideMenu(true);
        } else {
            setFileAdjustmentSideMenu(false);
        }
    }

    /** Applies slider values as a new {@link FileAdjustmentSettings} to the selected file. */
    @FXML
    private void onApplyFileAdjustments(ActionEvent e) {
        if (selectedFile == null || scanModel == null) return;
        try {
            int rotation = spinnerFileAdjustmentRotation.getValue();
            double hue = spinnerFileAdjustmentHue.getValue();
            double brightness = spinnerFileAdjustmentBrightness.getValue();
            double contrast = spinnerFileAdjustmentContrast.getValue();
            double saturation = spinnerFileAdjustmentSaturation.getValue();
            double sharpness = spinnerFileAdjustmentSharpness.getValue();

            // snapshot old settings into a copy before any mutation
            FileAdjustmentSettings oldSettings = new FileAdjustmentSettings(selectedFile.getRotation(), selectedFile.getHue(), selectedFile.getBrightness(), selectedFile.getContrast(), selectedFile.getSaturation(), selectedFile.getSharpness());
            FileAdjustmentSettings newSettings = new FileAdjustmentSettings(rotation, hue, brightness, contrast, saturation, sharpness);

            final File capturedFile = selectedFile;
            scanModel.updateFileSettings(capturedFile, newSettings);

            pushUndo(() -> {
                try {
                    scanModel.updateFileSettings(capturedFile, oldSettings);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            rebuildCard();
        } catch (IllegalArgumentException ex) {
            AlertHelper.showError("Invalid Input", ex.getMessage());
            // TODO add visual feedback
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
                modelFacade.getLogModel().createLog(new Log(currentUser, Integer.parseInt(scanModel.getTargetBox().getBoxName()), EntityType.BOX, LogAction.DELETE, LocalDateTime.now()));
                boxTreeView.setShowRoot(false);

                endScanSession();

                setSessionControlsDisabled(true);
                sessionPopupOverlay.setVisible(true);
                sessionPopupOverlay.setDisable(false);
                workspaceView.setDisable(true);
                lblSessionStatus.setText("Press Session Startup to configure and begin.");

                rebuild();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    /** Rotates the selected file by {@code spinnerRotation} degrees in the given direction. */
    private void rotatePage(int direction) {
        if (selectedFile == null || scanModel == null) return;

        int degrees = spinnerRotation.getValue() * direction;
        int oldRotation = selectedFile.getRotation();
        int newRotation = normalizeRotation(oldRotation + degrees);

        try {
            final File capturedFile = selectedFile;
            scanModel.updateFileRotation(capturedFile, newRotation);

            pushUndo(() -> {
                try {
                    scanModel.updateFileRotation(capturedFile, oldRotation);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            rebuildCard();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Rotation Failed", "Could not update file rotation.");
        }
    }

    // TreeView navigation
    @FXML private void onNavFirst(ActionEvent e) { navigateTo(0); }
    @FXML private void onNavPrev(ActionEvent e) { navigateTo(currentPageIndex() - 1); }
    @FXML private void onNavNext(ActionEvent e) { navigateTo(currentPageIndex() + 1); }
    @FXML private void onNavLast(ActionEvent e) { navigateTo(allPages().size() - 1); }

    private void navigateTo(int index) {
        List<File> all = allPages();
        if (all.isEmpty()) return;
        index = Math.clamp(index, 0, all.size() - 1);
        File target = all.get(index);
        for (Document document : documents) {
            if (document.getFiles().contains(target)) {
                selectPage(document, target);
                break;
            }
        }

        // prevents bugs with treeSelectionListener sometimes re-selecting the previous selection with onTreeSelectionChanged
        boxTreeView.getSelectionModel().selectedItemProperty().removeListener(treeSelectionListener);
        rebuildCard();
        syncTreeSelection();
        boxTreeView.getSelectionModel().selectedItemProperty().addListener(treeSelectionListener);
    }

    @FXML
    private void onZoomIn(ActionEvent e) {
        zoomLevel = Math.min(zoomLevel + ZOOM_STEP, ZOOM_MAX);
        rebuildCard();
    }

    @FXML
    private void onZoomOut(ActionEvent e) {
        zoomLevel = Math.max(zoomLevel - ZOOM_STEP, ZOOM_MIN);
        rebuildCard();
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
            // TODO add visual feedback
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Export Destination");
        java.io.File exportDirectory = chooser.showDialog(currentStage);
        if (exportDirectory == null) return; // user canceled

        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                scanModel.save();
                scanModel.export(exportDirectory, mode, progress -> updateProgress(progress * 100, 100));
                return null;
            }
        };

        progressBarExport.progressProperty().bind(exportTask.progressProperty());
        setExportInProgress(true);

        exportTask.setOnSucceeded(event -> {
            try {
                progressBarExport.progressProperty().unbind();
                setExportInProgress(false);
                modelFacade.getLogModel().createLog(new Log(currentUser, scanModel.getTargetBox().getBoxId(), EntityType.BOX, LogAction.EXPORT, LocalDateTime.now()));
                rebuild();
                AlertHelper.showInformation("Export Complete", "Export finished. \nFiles saved to:" + exportDirectory.getAbsolutePath());
            }
            catch (Exception exception) {
                AlertHelper.showInformation("Log Failed", "Log failed to be sent to database due to " + exception.getMessage());
            }
        });

        exportTask.setOnFailed(event -> {
            progressBarExport.progressProperty().unbind();
            setExportInProgress(false);
            rebuild();
            AlertHelper.showError("Export Failed", "Could not export documents. Please try again.");
        });

        Thread thread = new Thread(exportTask);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    private void onUndo(ActionEvent e) {
        if (undoStack.isEmpty()) return;
        undoStack.pop().run();
        btnUndo.setDisable(undoStack.isEmpty());
        rebuild();
    }

    private void pushUndo(Runnable inverse) {
        if (undoStack.size() >= maxUndos) undoStack.removeLast();
        undoStack.push(inverse);
        btnUndo.setDisable(false);
    }

    @FXML
    private void onDarkModeToggle() { ThemeHandler.toggle(currentStage.getScene(), darkMode.isSelected()); }

    @FXML
    private void onExit(ActionEvent actionEvent) {
        AlertHelper.showConfirmation("Exit Window", "Are you sure you want to exit?\n"
                + "Any unsaved progress will be deleted.", () -> {
            try {
                ViewHandler handler = currentUser.isAdmin() ? ViewHandler.ADMIN : ViewHandler.LOGIN;
                handler.reset();

                endScanSession();

                Stage stage = new Stage();
                if (!currentUser.isAdmin()) {
                    modelFacade.getSessionModel().logout();
                }
                else {
                    stage.setMinWidth(1366);
                    stage.setMinHeight(768);
                    stage.setMaximized(true);
                }
                handler.show(modelFacade, stage);
                currentStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Exit Error", "Failed to exit window. Please try again.");
            }
        });
    }

    private void onTreeSelectionChanged(TreeItem<TreeNode> item) {
        if (item == null || item.getValue() == null) return;

        TreeNode value = item.getValue();
        if (value instanceof Document document) {
            selectedDocument = document;
            selectedFile = null;
            selectedBox = null;
        } else if (value instanceof File file) {
            for (Document document : documents) {
                if (document.getFiles().contains(file)) {
                    selectPage(document, file);
                    break;
                }
            }
        } else if (value instanceof Box box) {
            selectedBox = box;
            selectedDocument = null;
            selectedFile = null;
        }

        rebuildCard();
    }

    /** Moves {@code file} to the end of {@code target}'s file list. */
    private boolean moveFileToDocument(File file, Document target) {
        Document source = findOwnerDocument(file);
        if (source == null || source == target) return false;

        final int originalIndex = source.getFiles().indexOf(file);
        source.getFiles().remove(file);
        target.getFiles().add(file);
        persistFileMoved(file, target);

        pushUndo(() -> {
            target.getFiles().remove(file);
            int restoreIdx = Math.min(originalIndex, source.getFiles().size());
            source.getFiles().add(restoreIdx, file);
            persistFileMoved(file, source);
        });

        return true;
    }

    /** Inserts {@code dragged} immediately before {@code targetFile}, possibly moving it to a different document. */
    private boolean moveFileBefore(File dragged, File targetFile) {
        if (dragged == targetFile) return false;

        Document draggedOwner = findOwnerDocument(dragged);
        Document targetOwner  = findOwnerDocument(targetFile);
        if (draggedOwner == null || targetOwner == null) return false;

        final int originalIndex = draggedOwner.getFiles().indexOf(dragged);
        final Document originalOwner = draggedOwner;

        draggedOwner.getFiles().remove(dragged);
        int insertIndex = targetOwner.getFiles().indexOf(targetFile);
        targetOwner.getFiles().add(insertIndex, dragged);
        persistFileMoved(dragged, targetOwner);

        pushUndo(() -> {
            targetOwner.getFiles().remove(dragged);
            int restoreIdx = Math.min(originalIndex, originalOwner.getFiles().size());
            originalOwner.getFiles().add(restoreIdx, dragged);
            persistFileMoved(dragged, originalOwner);
        });

        return true;
    }

    /** Moves {@code dragged} immediately before {@code target} in the document list. */
    private boolean reorderDocument(Document dragged, Document target) {
        if (dragged == target) return false;

        final int originalIndex = documents.indexOf(dragged);

        documents.remove(dragged);
        int insertIndex = documents.indexOf(target);
        documents.add(insertIndex, dragged);

        pushUndo(() -> {
            documents.remove(dragged);
            int restoreIdx = Math.min(originalIndex, documents.size());
            documents.add(restoreIdx, dragged);
        });

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
        rebuildCard();
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
    private void rebuildCard() {
        pageGrid.getChildren().clear();
        currentPreviewImageView = null;

        for (Document document : documents) {
            // if no box is selected and this document is not the selected document then skip it
            if (selectedBox == null && document != selectedDocument) continue;
            for (File file : document.getFiles()) {
                // if no file is selected and this file is not the selected file then skip it
                if (selectedFile != null && file != selectedFile) continue;
                pageGrid.getChildren().add(buildCard(document, file));
            }
        }

        if (selectedFile != null) {
            setFileAdjustmentBtn(true);
            if (fileAdjustmentSideMenu.isVisible()) {
                populateFileAdjustmentsFields(selectedFile);
            }
        } else {
            setFileAdjustmentSideMenu(false);
            setFileAdjustmentBtn(false);
        }

        updateCurrentPageLabel();
        updateCurrentDocumentLabel();
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
        lblTotalDocuments.setText("Documents: " + documents.size());
        lblTotalPages.setText("Files: " + totalPageCount());
    }

    private void updateCurrentPageLabel() {
        boolean show = selectedFile != null && selectedDocument != null;

        lblCurrentPage.setVisible(show);
        lblCurrentPage.setManaged(show);

        if (!show) return;

        int index = selectedDocument.getFiles().indexOf(selectedFile);
        int total = selectedDocument.getFiles().size();

        lblCurrentPage.setText(index >= 0 ? "File: " + (index + 1) + " / " + total : "File: 0 / 0");
    }

    private void updateCurrentDocumentLabel() {
        boolean show = selectedDocument != null && selectedBox == null;

        lblCurrentDocument.setVisible(show);
        lblCurrentDocument.setManaged(show);

        if (!show) return;

        int index = documents.indexOf(selectedDocument);
        lblCurrentDocument.setText(index >= 0 ? "Doc: " + (index + 1) : "Doc: 0");
    }

    private VBox buildCard(Document document, File file) {

        // image bounds inside the card (available space for the image inside the card)
        double maxImageWidth = cardWidth() - 8;
        double maxImageHeight = cardHeight() - 30;

        int rotation = file.getRotation();
        ImageView thumbnail = new ImageView();
        thumbnail.setPreserveRatio(true);
        thumbnail.setSmooth(true);
        // TODO set style with border outline
        thumbnail.setFitWidth(maxImageWidth);
        thumbnail.setFitHeight(maxImageHeight);
        thumbnail.setRotate(rotation);
        thumbnail.setEffect(new ColorAdjust(
                file.getHue() / 100.0,
                file.getSaturation() / 100.0,
                file.getBrightness() / 100.0,
                file.getContrast() / 100.0
        ));

        // clip container for preventing rotated image overflow
        StackPane previewPane = new StackPane(thumbnail);
        previewPane.setPrefSize(maxImageWidth, maxImageHeight);
        previewPane.setMinSize(maxImageWidth, maxImageHeight);
        previewPane.setMaxSize(maxImageWidth, maxImageHeight);
        previewPane.setClip(new Rectangle(maxImageWidth, maxImageHeight));

        // loading card
        Thread loader = new Thread(() -> {
            try {
                scanModel.loadImageData(file);
                Image image = file.getPreviewImage();

                if (file.getSharpness() != 0 && file.getImageData() != null) {
                    image = buildSharpImage(file, (float) (file.getSharpness() / 100f));
                }

                if (image != null && !image.isError()) {
                    final Image finalImage = image;

                    // bounding box of the image before any rotations
                    double originalWidth = image.getWidth();
                    double originalHeight = image.getHeight();

                    // rotation math helpers
                    double radians = Math.toRadians(rotation);
                    double cos = Math.abs(Math.cos(radians));
                    double sin = Math.abs(Math.sin(radians));

                    // bounding box after rotation (axis-aligned)
                    double rotatedBoundingWidth = originalWidth * cos + originalHeight * sin;
                    double rotatedBoundingHeight = originalWidth * sin + originalHeight * cos;

                    // scale factor to fit rotated image within constraints
                    double scale = Math.min(maxImageWidth / rotatedBoundingWidth, maxImageHeight / rotatedBoundingHeight);

                    // final displayed (rotated bounding box size in UI space)
                    double displayedWidth = rotatedBoundingWidth * scale;
                    double displayedHeight = rotatedBoundingHeight * scale;

                    // ImageView fit sizes (based on original image and not rotated bounds)
                    double imageViewFitWidth = originalWidth * scale;
                    double imageViewFitHeight = originalHeight * scale;

                    Platform.runLater(() -> {
                        thumbnail.setFitWidth(imageViewFitWidth);
                        thumbnail.setFitHeight(imageViewFitHeight);
                        // update the placeholder clip container
                        previewPane.setPrefSize(displayedWidth, displayedHeight);
                        previewPane.setMinSize(displayedWidth, displayedHeight);
                        previewPane.setMaxSize(displayedWidth, displayedHeight);
                        previewPane.setClip(new Rectangle(displayedWidth, displayedHeight));
                        thumbnail.setImage(finalImage);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loader.setDaemon(true); // runs in background and doesn't prevent closing the program mid-loading
        loader.start();

        boolean showFileLabel = (selectedBox != null || selectedDocument != null) && selectedFile == null;
        boolean showDocLabel = selectedBox != null && selectedFile == null;

        Label lblFile = new Label(setFileLabel(file));
        lblFile.getStyleClass().add("lbl");
        lblFile.setMaxWidth(cardWidth() - 8);
        lblFile.setVisible(showFileLabel);
        lblFile.setManaged(showFileLabel);

        Label lblDocument = new Label(setDocumentLabel(document));
        lblDocument.getStyleClass().add("lbl");
        lblDocument.setMaxWidth(cardWidth() - 8);
        lblDocument.setVisible(showDocLabel);
        lblDocument.setManaged(showDocLabel);

        VBox card = new VBox(4, previewPane, lblFile, lblDocument);
        card.setPrefWidth(cardWidth());
        card.setPrefHeight(cardHeight());
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("pageCard", "card-bg", "shadow");
        card.setPadding(new Insets(4));
        card.setUserData(file);

        if (file == selectedFile) { currentPreviewImageView = thumbnail; }

        card.setOnMouseClicked(event -> {
            // if file is already selected, then just return to avoid unnecessary rebuild
            if (file == selectedFile) {
                return;
            }

            //
            selectPage(document, file);
            updateCurrentPageLabel();
            updateCurrentDocumentLabel();
            rebuildCard();

            // prevents recursive tree selection events
            boxTreeView.getSelectionModel().selectedItemProperty().removeListener(treeSelectionListener);
            refreshTree();
            syncTreeSelection();
            boxTreeView.getSelectionModel().selectedItemProperty().addListener(treeSelectionListener);
        });

        if (file != selectedFile) {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(120), card);
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(120), card);

            card.setOnMouseEntered(e -> {
                scaleDown.stop();
                scaleUp.setToX(1.02); scaleUp.setToY(1.02);
                scaleUp.playFromStart();
            });
            card.setOnMouseExited(e -> {
                scaleUp.stop();
                scaleDown.setToX(1.0); scaleDown.setToY(1.0);
                scaleDown.playFromStart();
            });
        }

        return card;
    }

    // A4 ratio
    private double cardWidth() { return selectedFile != null ? 400 * zoomLevel : 380 * zoomLevel; }
    private double cardHeight() { return selectedFile != null ? 566 * zoomLevel : 538 * zoomLevel; }

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
        txtFldGlobalHue.setText(String.format("%.0f", settings.getHue()));
        txtFldGlobalBrightness.setText(String.format("%.0f", settings.getBrightness()));
        txtFldGlobalContrast.setText(String.format("%.0f", settings.getContrast()));
        txtFldGlobalSaturation.setText(String.format("%.0f", settings.getSaturation()));
    }

    private void populateFileAdjustmentsFields(File file) {
        spinnerFileAdjustmentRotation.getValueFactory().setValue(file.getRotation());
        spinnerFileAdjustmentHue.getValueFactory().setValue((int) file.getHue());
        spinnerFileAdjustmentBrightness.getValueFactory().setValue((int) file.getBrightness());
        spinnerFileAdjustmentContrast.getValueFactory().setValue((int) file.getContrast());
        spinnerFileAdjustmentSaturation.getValueFactory().setValue((int) file.getSaturation());
        spinnerFileAdjustmentSharpness.getValueFactory().setValue((int) file.getSharpness());
        applySharpnessToPreview(spinnerFileAdjustmentSharpness.getValue() / 100f);
    }

    private BufferedImage scaleToPreviewSize(BufferedImage source, int width, int height) {
        java.awt.Image scaled = source.getScaledInstance(width, height, java.awt.Image.SCALE_FAST);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.getGraphics().drawImage(scaled, 0, 0, null);
        return result;
    }

    /**
     * Wires a slider and spinner together so they stay in sync
     * and triggers a live preview on every slider change.
     *
     * @param slider the slider to listen on
     * @param spinner the spinner to keep in sync with the slider
     */
    private void bindSlider(Slider slider, Spinner<Integer> spinner) {
        slider.valueProperty().addListener((obs, o, newValue) -> {
            spinner.getValueFactory().setValue(newValue.intValue());
            applySpinnerPreview();
        });
        spinner.getEditor().textProperty().addListener((obs, o, newValue) -> {
            try {
                slider.setValue(Integer.parseInt(newValue));
            } catch (NumberFormatException ignored) {}
        });
    }

    /**
     * Applies the current spinner values as a live preview on the selected File's
     * image without persisting anything to the File object.
     */
    private void applySpinnerPreview() {
        if (!fileAdjustmentSideMenu.isVisible() || currentPreviewImageView == null) return;

        currentPreviewImageView.setEffect(new ColorAdjust(
                spinnerFileAdjustmentHue.getValue() / 100.0,
                spinnerFileAdjustmentSaturation.getValue() / 100.0,
                spinnerFileAdjustmentBrightness.getValue() / 100.0,
                spinnerFileAdjustmentContrast.getValue() / 100.0
        ));
    }

    private void applySharpnessToPreview(float strength) {
        if (selectedFile == null || currentPreviewImageView == null || selectedFile.getImageData() == null) return;
        try {
            Image image = buildSharpImage(selectedFile, strength);
            if (!image.isError()) {
                currentPreviewImageView.setImage(image);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Image buildSharpImage(File file, float strength) throws IOException {
        BufferedImage scaled = ImageIO.read(new ByteArrayInputStream(file.getImageData()));

        BufferedImage preview = scanModel.sharpen(scaleToPreviewSize(scaled, (int) cardWidth() - 8, (int) cardHeight() - 44), strength);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(preview, "png", out);

        return new Image(new ByteArrayInputStream(out.toByteArray()));
    }

    private void selectPage(Document document, File file) {
        selectedDocument = document;
        selectedFile = file;
        selectedBox = null;
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
        btnUndo.setDisable(disabled);
        btnExport.setDisable(disabled);
        btnZoomOut.setDisable(disabled);
        btnZoomIn.setDisable(disabled);
        btnRotLeft.setDisable(disabled);
        btnRotRight.setDisable(disabled);
    }

    private void setFileAdjustmentSideMenu(boolean disabled) {
        fileAdjustmentSideMenu.setVisible(disabled);
        fileAdjustmentSideMenu.setManaged(disabled);
    }

    private void setFileAdjustmentBtn(boolean disabled) {
        btnFileAdjustments.setVisible(disabled);
        btnFileAdjustments.setManaged(disabled);
    }

    private void setExportInProgress(boolean inProgress) {
        hboxProgressBarExport.setVisible(inProgress);
        hboxProgressBarExport.setManaged(inProgress);
    }

    private String setDocumentLabel(Document document) {
        int position = documents.indexOf(document) + 1;
        return "Document " + position;
    }

    private String setFileLabel(File file) {
        for (Document document : documents) {
            int position = document.getFiles().indexOf(file);
            if (position >= 0) {
                return "Page " + (position + 1);
            }
        }
        return "Page ?"; // should not happen :o
    }

    private int currentPageIndex() {
        if (selectedFile == null) return -1;
        return allPages().indexOf(selectedFile);
    }

    private Document findOwnerDocument(File file) {
        return documents.stream().filter(document -> document.getFiles().contains(file)).findFirst().orElse(null);
    }

    private int normalizeRotation(int rotation) {
        return ((rotation % 360) + 360) % 360;
    }
}