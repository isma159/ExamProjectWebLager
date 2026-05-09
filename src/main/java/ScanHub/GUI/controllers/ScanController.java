package ScanHub.GUI.controllers;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.Profile;
import ScanHub.BLL.BoxManager;
import ScanHub.BLL.ScanManager;
import ScanHub.DAL.ApiClient.ScanApiClient;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ScanController implements Initializable, IViewController {

    @FXML private Label sessionStatusLabel;
    @FXML private Label currentUserLabel;
    @FXML private Label scanSourceLabel;
    @FXML private Button btnScan, btnStop, btnRotLeft, btnRotRight, btnNewDoc, btnDelete, btnUndo, btnExport;
    @FXML private ComboBox<String> exportModeComboBox;
    @FXML private Label pageInfoLabel;
    @FXML private ComboBox<String> gridModeComboBox;
    @FXML private Spinner<Integer> globalRotSpinner;
    @FXML private TreeView<String> documentTreeView;
    @FXML private Pane flashOverlay;
    @FXML private Label barcodeToast;
    @FXML private ScrollPane pageScrollPane;
    @FXML private FlowPane pageGrid;
    @FXML private Label emptyStateLabel;
    @FXML private Label stDocsLabel;
    @FXML private Label stPagesLabel;
    @FXML private Label stCurrentLabel;
    @FXML private Label stExportLabel;
    @FXML private StackPane sessionPopupOverlay;
    @FXML private ComboBox<Profile> profileComboBox;
    @FXML private TextField boxIdField;
    @FXML private Spinner<Integer> rotationSpinner;
    @FXML private Label apiCountLabel;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private final ObservableList<Document> documents = FXCollections.observableArrayList();
    private Document selectedDocument;
    private File selectedPage;
    private ScanManager scanManager;
    private Box activeBox;
    private boolean sessionActive;
    private int currentPageIndex = -1;
    private double zoomLevel = 1.0;

    private static final double ZOOM_STEP = 0.15;
    private static final double ZOOM_MIN = 0.40;
    private static final double ZOOM_MAX = 3.00;

    @Override
    public void setModel(ModelFacade model, Stage stage) {
        this.modelFacade = model;
        this.currentStage = stage;
        initProfileComboBox();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initExportComboBoxes();
        initGridModeComboBox();
        initSpinners();
        initDocumentTree();
        initKeyboardShortcuts();

        setSessionControlsDisabled(true);
        sessionStatusLabel.setText("Press Session Startup to configure and begin.");
        refreshStatusBar();
        updatePageInfoLabel();
    }

    private void initProfileComboBox() {
        if (modelFacade == null) {
            return;
        }

        profileComboBox.setItems(modelFacade.getProfileModel().getProfiles());
        if (!profileComboBox.getItems().isEmpty()) {
            profileComboBox.getSelectionModel().selectFirst();
        }
    }

    private void initExportComboBoxes() {
        exportModeComboBox.setItems(FXCollections.observableArrayList("Single-page TIFF", "Multi-page TIFF"));
        exportModeComboBox.getSelectionModel().selectFirst();
    }

    private void initGridModeComboBox() {
        gridModeComboBox.setItems(FXCollections.observableArrayList("1 column", "2 columns", "3 columns", "4 columns"));
        gridModeComboBox.getSelectionModel().select(1);
        gridModeComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> applyGridMode(newValue.intValue()));
    }

    private void initSpinners() {
        globalRotSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-270, 270, 0, 90));
        rotationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-270, 270, 0, 90));
    }

    private void initDocumentTree() {
        TreeItem<String> root = new TreeItem<>("root");
        documentTreeView.setRoot(root);
        documentTreeView.setShowRoot(false);
        documentTreeView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> onTreeSelectionChanged(newValue));

        documentTreeView.setCellFactory(tv -> new javafx.scene.control.TreeCell<>() {
            {
                setOnDragDetected(event -> {
                    TreeItem<String> item = getTreeItem();
                    if (item == null || item == documentTreeView.getRoot()) return;

                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(item.getValue());
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
                    Dragboard db = event.getDragboard();
                    if (!db.hasString()) return;

                    String draggedLabel = db.getString();
                    TreeItem<String> targetItem = getTreeItem();
                    if (targetItem == null) return;

                    boolean success = false;

                    if (draggedLabel.startsWith("File #") && targetItem.getValue().startsWith("Document #")) {
                        // File → Document: move file into this document
                        success = moveFileToDococument(draggedLabel, targetItem.getValue());

                    } else if (draggedLabel.startsWith("File #") && targetItem.getValue().startsWith("File #")) {
                        // File → File: move file before the target file's position
                        success = moveFileBefore(draggedLabel, targetItem.getValue());

                    } else if (draggedLabel.startsWith("Document #") && targetItem.getValue().startsWith("Document #")) {
                        // Document → Document: reorder
                        success = reorderDocument(draggedLabel, targetItem.getValue());
                    }

                    event.setDropCompleted(success);
                    event.consume();
                    if (success) rebuild();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
    }

// --- Drag-and-drop logic ---

    private boolean moveFileToDococument(String fileLabel, String docLabel) {
        File file = findFileByLabel(fileLabel);
        Document target = findDocumentByLabel(docLabel);
        if (file == null || target == null) return false;

        Document source = findOwnerDocument(file);
        if (source == null || source == target) return false;

        source.getFiles().remove(file);
        target.getFiles().add(file);
        persistFileMoved(file, target);
        return true;
    }

    private boolean moveFileBefore(String draggedLabel, String targetLabel) {
        File dragged = findFileByLabel(draggedLabel);
        File targetFile = findFileByLabel(targetLabel);
        if (dragged == null || targetFile == null || dragged == targetFile) return false;

        Document draggedOwner = findOwnerDocument(dragged);
        Document targetOwner = findOwnerDocument(targetFile);
        if (draggedOwner == null || targetOwner == null) return false;

        draggedOwner.getFiles().remove(dragged);
        int insertIndex = targetOwner.getFiles().indexOf(targetFile);
        targetOwner.getFiles().add(insertIndex, dragged);
        persistFileMoved(dragged, targetOwner);
        return true;
    }

    private boolean reorderDocument(String draggedLabel, String targetLabel) {
        Document dragged = findDocumentByLabel(draggedLabel);
        Document target = findDocumentByLabel(targetLabel);
        if (dragged == null || target == null || dragged == target) return false;

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

    private File findFileByLabel(String label) {
        int id = parseTrailingId(label);
        return documents.stream()
                .flatMap(d -> d.getFiles().stream())
                .filter(f -> f.getFileId() == id)
                .findFirst().orElse(null);
    }

    private Document findDocumentByLabel(String label) {
        int id = parseTrailingId(label);
        return documents.stream()
                .filter(d -> d.getDocumentId() == id)
                .findFirst().orElse(null);
    }

    private Document findOwnerDocument(File file) {
        return documents.stream()
                .filter(d -> d.getFiles().contains(file))
                .findFirst().orElse(null);
    }

    private void initKeyboardShortcuts() {
        pageGrid.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;
            scene.setOnKeyPressed(e -> {
                KeyCode code = e.getCode();
                if (code == KeyCode.SPACE) { onScan(null); e.consume(); }
                else if (code == KeyCode.LEFT) { onNavPrev(null); e.consume(); }
                else if (code == KeyCode.RIGHT) { onNavNext(null); e.consume(); }
                else if (code == KeyCode.HOME) { onNavFirst(null); e.consume(); }
                else if (code == KeyCode.END) { onNavLast(null); e.consume(); }
                else if (code == KeyCode.OPEN_BRACKET) { onRotateLeft(null); e.consume(); }
                else if (code == KeyCode.CLOSE_BRACKET) { onRotateRight(null); e.consume(); }
                else if (code == KeyCode.DELETE) { onDeletePage(null); e.consume(); }
                else if (code == KeyCode.N && !e.isControlDown()) { onNewDocument(null); e.consume(); }
                else if (code == KeyCode.E && e.isControlDown()) { onExport(null); e.consume(); }
            });
        });
    }

    @FXML
    private void onSessionStartup(ActionEvent e) {
        initProfileComboBox();
        sessionPopupOverlay.setVisible(true);
    }

    @FXML
    private void onSessionPopupClose(ActionEvent e) {
        sessionPopupOverlay.setVisible(false);
    }

    @FXML
    private void onSessionPopupBackdropClick(MouseEvent e) {
        sessionPopupOverlay.setVisible(false);
    }

    @FXML
    private void onSessionPopupConsumeClick(MouseEvent e) {
        e.consume();
    }

    @FXML
    private void onStartSession(ActionEvent e) {
        Profile profile = profileComboBox.getValue();
        String boxInput = boxIdField.getText().trim();

        if (profile == null) {
            AlertHelper.showError("Session Setup", "Please select a profile before starting.");
            return;
        }
        if (boxInput.isEmpty()) {
            AlertHelper.showError("Session Setup", "Please enter a Box ID before starting.");
            return;
        }

        try {
            BoxManager boxManager = new BoxManager();
            activeBox = boxManager.getOrCreateSessionBox(boxInput, profile);
            scanManager = new ScanManager(new ScanApiClient(), activeBox);
            syncDocumentsFromManager();

            globalRotSpinner.getValueFactory().setValue(rotationSpinner.getValue());
            sessionActive = true;
            selectedDocument = null;
            selectedPage = null;

            setSessionControlsDisabled(false);
            sessionStatusLabel.setText("Session active - Profile: " + profile.getProfileName() + " - Box: " + activeBox.getBoxName());
            currentUserLabel.setText(profile.getProfileName());
            scanSourceLabel.setText("Box #" + activeBox.getBoxId());
            sessionPopupOverlay.setVisible(false);
            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Session Setup", "Could not start scan session.");
        }
    }

    @FXML
    private void onScan(ActionEvent e) {
        if (!sessionActive || scanManager == null) return;

        try {
            ScanManager.StoredScan result = scanManager.fetchAndStore(globalRotSpinner.getValue());
            syncDocumentsFromManager();

            if (result.barcodeSplit()) {
                selectedDocument = result.document();
                selectedPage = null;
                showBarcodeToast();
                rebuild();
                return;
            }

            selectPage(result.document(), result.file());
            rebuild();
            playFlashAnimation();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Scan Failed", "Could not scan and store the next page.");
        }
    }

    @FXML
    private void onStop(ActionEvent e) {
        sessionStatusLabel.setText("Scanning stopped.");
    }

    @FXML
    private void onNewDocument(ActionEvent e) {
        if (!sessionActive || scanManager == null) return;

        try {
            selectedDocument = scanManager.manualSplit();
            selectedPage = null;
            syncDocumentsFromManager();
            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("New Document Failed", "Could not create a new document.");
        }
    }

    @FXML
    private void onDeletePage(ActionEvent e) {
        if (selectedDocument == null || selectedPage == null || scanManager == null) return;

        try {
            File removed = selectedPage;
            scanManager.deleteFile(removed);
            selectedDocument.getFiles().removeIf(file -> file.getFileId() == removed.getFileId());
            selectedPage = null;
            syncDocumentsFromManager();
            rebuild();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Delete Failed", "Could not delete the selected page.");
        }
    }

    @FXML private void onRotateLeft(ActionEvent e) { rotatePage(-90); }
    @FXML private void onRotateRight(ActionEvent e) { rotatePage(90); }

    private void rotatePage(int delta) {
        if (selectedPage == null || scanManager == null) return;

        int rotation = normalizeRotation(selectedPage.getRotation() + delta);
        try {
            scanManager.updateFileRotation(selectedPage, rotation);
            rebuildPageGrid();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showError("Rotation Failed", "Could not update page rotation.");
        }
    }

    @FXML private void onNavFirst(ActionEvent e) { navigateTo(0); }
    @FXML private void onNavPrev(ActionEvent e)  { navigateTo(currentPageIndex - 1); }
    @FXML private void onNavNext(ActionEvent e)  { navigateTo(currentPageIndex + 1); }
    @FXML private void onNavLast(ActionEvent e)  { navigateTo(allPages().size() - 1); }

    private void navigateTo(int index) {
        List<File> all = allPages();
        if (all.isEmpty()) return;

        index = Math.max(0, Math.min(index, all.size() - 1));
        selectedPage = all.get(index);
        currentPageIndex = index;

        for (Document document : documents) {
            if (document.getFiles().contains(selectedPage)) {
                selectedDocument = document;
                break;
            }
        }

        rebuildPageGrid();
    }

    @FXML
    private void onZoomIn(ActionEvent e) {
        zoomLevel = Math.min(zoomLevel + ZOOM_STEP, ZOOM_MAX);
        rebuildPageGrid();
    }

    @FXML
    private void onZoomOut(ActionEvent e) {
        zoomLevel = Math.max(zoomLevel - ZOOM_STEP, ZOOM_MIN);
        rebuildPageGrid();
    }

    private void applyGridMode(int modeIndex) {
        double gap = 16;
        double baseCard = 160 * zoomLevel;
        int columns = modeIndex + 1;
        pageGrid.setPrefWrapLength(columns * (baseCard + gap));
    }

    @FXML
    private void onExport(ActionEvent e) {
        if (documents.isEmpty() || totalPageCount() == 0) {
            AlertHelper.showError("Export", "There are no document to export.");
            return;
        }

        // TODO: make exporting
    }

    @FXML // TODO: Delete
    private void onRefreshApiCount(ActionEvent e) {
        apiCountLabel.setText("API calls: connected on scan");
    }

    @FXML
    private void onUndo(ActionEvent e) { // TODO make undo

    }

    @FXML
    private void onExit(ActionEvent actionEvent) {
        AlertHelper.showConfirmation("Exit Window", "Are you sure you want to exit?", () -> {
            try {
                ViewHandler handler = ViewHandler.LOGIN;
                handler.reset();
                handler.show(modelFacade);
                currentStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Exit Error", "Failed to exit window. Please try again.");
            }
        });
    }

    private void onTreeSelectionChanged(TreeItem<String> item) {
        if (item == null || item.getParent() == null) return;

        String value = item.getValue();
        if (value.startsWith("Document #")) {
            int documentId = parseTrailingId(value);
            documents.stream()
                    .filter(document -> document.getDocumentId() == documentId)
                    .findFirst()
                    .ifPresent(document -> {
                        selectedDocument = document;
                        selectedPage = document.getFiles().isEmpty() ? null : document.getFiles().get(0);
                    });
        } else if (value.startsWith("File #")) {
            int fileId = parseTrailingId(value);
            for (Document document : documents) {
                for (File file : document.getFiles()) {
                    if (file.getFileId() == fileId) {
                        selectPage(document, file);
                        break;
                    }
                }
            }
        }

        rebuildPageGrid();
    }

    private void rebuildPageGrid() {
        pageGrid.getChildren().clear();

        if (selectedDocument != null && selectedPage != null) {
            pageGrid.getChildren().add(buildPageCard(selectedDocument, selectedPage));
        }

        applyGridMode(gridModeComboBox.getSelectionModel().getSelectedIndex());
        highlightSelectedCard();
        updatePageInfoLabel();
        emptyStateLabel.setVisible(selectedPage == null);
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
            highlightSelectedCard();
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
        } catch (Exception ignored) {
        }

        return new Image(new ByteArrayInputStream(imageData), width, height, true, true);
    }

    private void refreshTree() {
        TreeItem<String> root = documentTreeView.getRoot();
        root.getChildren().clear();

        for (Document document : documents) {
            TreeItem<String> docItem = new TreeItem<>(documentLabel(document));
            docItem.setExpanded(true);
            for (File file : document.getFiles()) {
                docItem.getChildren().add(makeDraggableFileItem(file, document));
            }
            root.getChildren().add(makeDraggableDocItem(docItem, document));
        }
    }

    private TreeItem<String> makeDraggableDocItem(TreeItem<String> docItem, Document document) {
        // We use a custom cell factory below — the drag source/target logic lives there
        return docItem;
    }

    private TreeItem<String> makeDraggableFileItem(File file, Document ownerDocument) {
        return new TreeItem<>(fileLabel(file));
    }

    private void refreshStatusBar() {
        stDocsLabel.setText("Docs: " + documents.size());
        stPagesLabel.setText("Pages: " + totalPageCount());
        stCurrentLabel.setText(selectedDocument != null ? documentLabel(selectedDocument) : "-");
    }

    private void updatePageInfoLabel() {
        List<File> all = allPages();
        if (all.isEmpty()) {
            pageInfoLabel.setText("0 / 0");
            currentPageIndex = -1;
            return;
        }
        currentPageIndex = selectedPage != null ? all.indexOf(selectedPage) : 0;
        if (currentPageIndex < 0) currentPageIndex = 0;
        pageInfoLabel.setText((currentPageIndex + 1) + " / " + all.size());
    }

    private void rebuild() {
        rebuildPageGrid();
        refreshTree();
        refreshStatusBar();
    }

    private void selectPage(Document document, File file) {
        selectedDocument = document;
        selectedPage = file;
        currentPageIndex = allPages().indexOf(file);
        stCurrentLabel.setText(documentLabel(document) + " - " + fileLabel(file));
    }

    private void syncDocumentsFromManager() {
        documents.setAll(scanManager.getTargetBox().getDocuments());
    }

    private List<File> allPages() {
        List<File> files = new ArrayList<>();
        for (Document document : documents) {
            files.addAll(document.getFiles());
        }
        return files;
    }

    private int totalPageCount() {
        return documents.stream().mapToInt(document -> document.getFiles().size()).sum();
    }

    private void highlightSelectedCard() {
        for (Node node : pageGrid.getChildren()) {
            if (node instanceof VBox card) {
                boolean selected = card.getUserData() == selectedPage;
                card.setStyle(selected ? "-fx-border-color:#4a9eff; -fx-border-width:2; -fx-border-radius:4;" : "");
            }
        }
    }

    private double cardWidth() {
        return 520 * zoomLevel;
    }

    private double cardHeight() {
        return 700 * zoomLevel;
    }

    private void setSessionControlsDisabled(boolean disabled) {
        btnScan.setDisable(disabled);
        btnStop.setDisable(disabled);
        btnRotLeft.setDisable(disabled);
        btnRotRight.setDisable(disabled);
        btnNewDoc.setDisable(disabled);
        btnDelete.setDisable(disabled);
        btnUndo.setDisable(disabled);
        btnExport.setDisable(disabled);
    }

    private void playFlashAnimation() {
        flashOverlay.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(400), flashOverlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(event -> flashOverlay.setVisible(false));
        fade.play();
    }

    private void showBarcodeToast() {
        barcodeToast.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(1400), barcodeToast);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(event -> {
            barcodeToast.setOpacity(1.0);
            barcodeToast.setVisible(false);
        });
        fade.play();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String documentLabel(Document document) {
        return "Document #" + document.getDocumentId();
    }

    private String fileLabel(File file) {
        return "File #" + file.getFileId();
    }

    private int parseTrailingId(String text) {
        int index = text.lastIndexOf('#');
        if (index < 0 || index + 1 >= text.length()) {
            return -1;
        }
        try {
            return Integer.parseInt(text.substring(index + 1).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int normalizeRotation(int rotation) {
        int normalized = ((rotation % 360) + 360) % 360;
        return switch (normalized) {
            case 90, 180, 270 -> normalized;
            default -> 0;
        };
    }
}
