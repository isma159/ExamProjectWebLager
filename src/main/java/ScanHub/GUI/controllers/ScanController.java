package ScanHub.GUI.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

/**
 * Controller for ScanView.fxml.
 *
 * Responsibilities
 * ─────────────────
 *  • Session startup popup (profile, box ID, global rotation)
 *  • Scan / stop / undo
 *  • Document + page management (create, delete, reorder, rotate)
 *  • Page grid (FlowPane) with card rendering and zoom
 *  • Document tree (left sidebar)
 *  • Keyboard shortcuts
 *  • Status bar + page-info label
 *  • Export
 *
 * TODOs are marked where real service / repository calls should go.
 */
public class ScanController implements Initializable {

    // =========================================================================
    // FXML injections
    // =========================================================================

    // — Header —
    @FXML private HBox  hboxHeader;
    @FXML private Label sessionStatusLabel;
    @FXML private Label currentUserLabel;
    @FXML private Label scanSourceLabel;

    // — Main toolbar —
    @FXML private HBox mainToolbar;
    @FXML private Button btnSessionStartup, btnScan, btnStop, btnRotLeft, btnRotRight, btnNewDoc, btnDelete, btnUndo, btnExport;
    @FXML private ComboBox<String>  exportModeComboBox;

    // — Sub-toolbar —
    @FXML private HBox              subToolbar;
    @FXML private Button            btnNavFirst;
    @FXML private Button            btnNavPrev;
    @FXML private Label             pageInfoLabel;
    @FXML private Button            btnNavNext;
    @FXML private Button            btnNavLast;
    @FXML private Button            btnZoomOut;
    @FXML private Button            btnZoomIn;
    @FXML private ComboBox<String>  gridModeComboBox;
    @FXML private Spinner<Integer>  globalRotSpinner;

    // — Left sidebar —
    @FXML private Label                treeStatsLabel;
    @FXML private TreeView<String>     documentTreeView;

    // — Center —
    @FXML private Pane       flashOverlay;
    @FXML private Label      barcodeToast;
    @FXML private ScrollPane pageScrollPane;
    @FXML private FlowPane   pageGrid;
    @FXML private Label      emptyStateLabel;

    // — Status bar —
    @FXML private Label stDocsLabel;
    @FXML private Label stPagesLabel;
    @FXML private Label stCurrentLabel;
    @FXML private Label stExportLabel;

    // — Session popup —
    @FXML private StackPane            sessionPopupOverlay;
    @FXML private VBox                 vboxSessionSetup;
    @FXML private ComboBox<String>     profileComboBox;
    @FXML private TextField            boxIdField;
    @FXML private Spinner<Integer>     rotationSpinner;
    @FXML private Label                apiCountLabel;
    @FXML private ComboBox<String>     popupExportModeComboBox;

    // =========================================================================
    // State
    // =========================================================================

    private final ObservableList<Document> documents = FXCollections.observableArrayList();

    private Document selectedDocument  = null;
    private ScanPage selectedPage      = null;
    private int      currentPageIndex  = -1;

    private boolean sessionActive = false;

    /** Zoom multiplier applied to all page cards in the FlowPane. */
    private double zoomLevel = 1.0;
    private static final double ZOOM_STEP = 0.15;
    private static final double ZOOM_MIN  = 0.40;
    private static final double ZOOM_MAX  = 3.00;

    /**
     * Undo stack — each entry is a {@link Runnable} that reverses the
     * most recently committed action.  Max depth: {@value #UNDO_MAX}.
     */
    private final Deque<Runnable> undoStack = new ArrayDeque<>();
    private static final int UNDO_MAX = 20;

    // =========================================================================
    // Initializable
    // =========================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initProfileComboBox();
        initExportComboBoxes();
        initGridModeComboBox();
        initSpinners();
        initDocumentTree();
        initKeyboardShortcuts();

        setSessionControlsDisabled(true);
        sessionStatusLabel.setText("Press ⚙ Session Startup to configure and begin.");
        refreshStatusBar();
        updatePageInfoLabel();
    }

    // =========================================================================
    // Init helpers
    // =========================================================================

    private void initProfileComboBox() {
        // TODO: replace with ProfileRepository.findAll() or equivalent service call
        profileComboBox.setItems(FXCollections.observableArrayList(
                "Default Profile", "Archive A4", "Archive Legal", "Scan-To-Text"
        ));
        profileComboBox.getSelectionModel().selectFirst();
    }

    private void initExportComboBoxes() {
        ObservableList<String> modes = FXCollections.observableArrayList(
                "Single TIFF per document",
                "Multi-page TIFF",
                "PDF per document",
                "ZIP of TIFFs"
        );
        exportModeComboBox.setItems(modes);
        exportModeComboBox.getSelectionModel().selectFirst();

        popupExportModeComboBox.setItems(modes);
        popupExportModeComboBox.getSelectionModel().selectFirst();

        // Keep the toolbar and popup pickers in sync
        exportModeComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) ->
                        popupExportModeComboBox.getSelectionModel().select(n.intValue()));
        popupExportModeComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) ->
                        exportModeComboBox.getSelectionModel().select(n.intValue()));
    }

    private void initGridModeComboBox() {
        gridModeComboBox.setItems(FXCollections.observableArrayList(
                "1 column", "2 columns", "3 columns", "4 columns"
        ));
        gridModeComboBox.getSelectionModel().select(1);   // default: 2 columns
        gridModeComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> applyGridMode(n.intValue()));
    }

    private void initSpinners() {
        globalRotSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-270, 270, 0, 90));
        rotationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-270, 270, 0, 90));
    }

    private void initDocumentTree() {
        TreeItem<String> root = new TreeItem<>("root");
        documentTreeView.setRoot(root);
        documentTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> onTreeSelectionChanged(newVal));
    }

    /**
     * Keyboard shortcuts are registered on the Scene once it becomes
     * available (not yet at {@code initialize()} time).
     */
    private void initKeyboardShortcuts() {
        pageGrid.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;
            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case SPACE            -> { onScan(null);         e.consume(); }
                    case LEFT             -> { onNavPrev(null);      e.consume(); }
                    case RIGHT            -> { onNavNext(null);      e.consume(); }
                    case HOME             -> { onNavFirst(null);     e.consume(); }
                    case END              -> { onNavLast(null);      e.consume(); }
                    case OPEN_BRACKET     -> { onRotateLeft(null);   e.consume(); }
                    case CLOSE_BRACKET    -> { onRotateRight(null);  e.consume(); }
                    case DELETE           -> { onDeletePage(null);   e.consume(); }
                    case N -> {
                        if (!e.isControlDown()) { onNewDocument(null); e.consume(); }
                    }
                    case Z -> {
                        if (e.isControlDown()) { onUndo(null); e.consume(); }
                    }
                    case E -> {
                        if (e.isControlDown()) { onExport(null); e.consume(); }
                    }
                    default -> { /* ignored */ }
                }
            });
        });
    }

    // =========================================================================
    // Session popup
    // =========================================================================

    /** Opens the Session Startup popup (⚙ button in main toolbar). */
    @FXML
    private void onSessionStartup(ActionEvent e) {
        sessionPopupOverlay.setVisible(true);
    }

    /** ✕ close button inside the popup card. */
    @FXML
    private void onSessionPopupClose(ActionEvent e) {
        sessionPopupOverlay.setVisible(false);
    }

    /** Click on the semi-transparent backdrop closes the popup. */
    @FXML
    private void onSessionPopupBackdropClick(MouseEvent e) {
        sessionPopupOverlay.setVisible(false);
    }

    /**
     * Click anywhere inside the card is consumed so it does NOT bubble
     * up to the backdrop handler and accidentally close the popup.
     */
    @FXML
    private void onSessionPopupConsumeClick(MouseEvent e) {
        e.consume();
    }

    // =========================================================================
    // Session start
    // =========================================================================

    @FXML
    private void onStartSession(ActionEvent e) {
        String profileName = profileComboBox.getValue();
        String boxId       = boxIdField.getText().trim();

        if (profileName == null || profileName.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Session Setup",
                    "Please select a profile before starting.");
            return;
        }
        if (boxId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Session Setup",
                    "Please enter a Box ID before starting.");
            return;
        }

        // Mirror popup rotation spinner → sub-toolbar spinner
        globalRotSpinner.getValueFactory().setValue(rotationSpinner.getValue());

        sessionActive = true;
        setSessionControlsDisabled(false);

        sessionStatusLabel.setText(
                "Session active  ·  Profile: " + profileName + "  ·  Box: " + boxId);
        currentUserLabel.setText(profileName);   // TODO: use logged-in username
        scanSourceLabel.setText("Box: " + boxId);

        sessionPopupOverlay.setVisible(false);
        refreshStatusBar();
    }

    // =========================================================================
    // Scanning
    // =========================================================================

    @FXML
    private void onScan(ActionEvent e) {
        if (!sessionActive) return;

        // TODO: replace with real ScanService.acquirePage() call
        if (documents.isEmpty() || selectedDocument == null) {
            createNewDocumentInternal();
        }

        ScanPage page   = new ScanPage(
                "Page " + (totalPageCount() + 1),
                globalRotSpinner.getValue());
        Document target = selectedDocument;

        target.getPages().add(page);
        pushUndo(() -> {
            target.getPages().remove(page);
            rebuild();
        });

        selectPage(target, page);
        rebuild();
        playFlashAnimation();
    }

    @FXML
    private void onStop(ActionEvent e) {
        // TODO: ScanService.stop()
        sessionStatusLabel.setText("Scanning stopped.");
    }

    // =========================================================================
    // Document management
    // =========================================================================

    @FXML
    private void onNewDocument(ActionEvent e) {
        if (!sessionActive) return;
        createNewDocumentInternal();
        rebuild();
    }

    private void createNewDocumentInternal() {
        Document doc = new Document("Document " + (documents.size() + 1));
        documents.add(doc);
        selectedDocument = doc;
        pushUndo(() -> {
            documents.remove(doc);
            if (selectedDocument == doc) {
                selectedDocument = documents.isEmpty()
                        ? null
                        : documents.get(documents.size() - 1);
            }
            rebuild();
        });
    }

    // =========================================================================
    // Page actions
    // =========================================================================

    @FXML
    private void onDeletePage(ActionEvent e) {
        if (selectedDocument == null || selectedPage == null) return;

        int idx = selectedDocument.getPages().indexOf(selectedPage);
        if (idx < 0) return;

        ScanPage removed    = selectedPage;
        Document ownerDoc   = selectedDocument;

        ownerDoc.getPages().remove(idx);
        pushUndo(() -> {
            ownerDoc.getPages().add(Math.min(idx, ownerDoc.getPages().size()), removed);
            rebuild();
        });

        // Select the nearest remaining page
        if (!ownerDoc.getPages().isEmpty()) {
            int next = Math.min(idx, ownerDoc.getPages().size() - 1);
            selectPage(ownerDoc, ownerDoc.getPages().get(next));
        } else {
            selectedPage     = null;
            currentPageIndex = -1;
        }

        rebuild();

        if (totalPageCount() == 0) emptyStateLabel.setVisible(true);
    }

    @FXML private void onRotateLeft(ActionEvent e)  { rotatePage(-90); }
    @FXML private void onRotateRight(ActionEvent e) { rotatePage(90);  }

    private void rotatePage(int delta) {
        if (selectedPage == null) return;
        int prev = selectedPage.getRotation();
        selectedPage.setRotation((prev + delta + 360) % 360);
        pushUndo(() -> { selectedPage.setRotation(prev); rebuildPageGrid(); });
        rebuildPageGrid();
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    @FXML private void onNavFirst(ActionEvent e) { navigateTo(0); }
    @FXML private void onNavPrev(ActionEvent e)  { navigateTo(currentPageIndex - 1); }
    @FXML private void onNavNext(ActionEvent e)  { navigateTo(currentPageIndex + 1); }
    @FXML private void onNavLast(ActionEvent e)  { navigateTo(allPages().size() - 1); }

    private void navigateTo(int index) {
        List<ScanPage> all = allPages();
        if (all.isEmpty()) return;

        index        = Math.max(0, Math.min(index, all.size() - 1));
        selectedPage = all.get(index);
        currentPageIndex = index;

        // Find the owning document
        for (Document doc : documents) {
            if (doc.getPages().contains(selectedPage)) {
                selectedDocument = doc;
                break;
            }
        }

        highlightSelectedCard();
        updatePageInfoLabel();
    }

    // =========================================================================
    // Zoom
    // =========================================================================

    @FXML
    private void onZoomIn(ActionEvent e) {
        zoomLevel = Math.min(zoomLevel + ZOOM_STEP, ZOOM_MAX);
        applyZoom();
    }

    @FXML
    private void onZoomOut(ActionEvent e) {
        zoomLevel = Math.max(zoomLevel - ZOOM_STEP, ZOOM_MIN);
        applyZoom();
    }

    private void applyZoom() {
        double cardW = cardWidth();
        double cardH = cardHeight();
        for (Node node : pageGrid.getChildren()) {
            if (node instanceof VBox card) {
                card.setPrefWidth(cardW);
                card.setPrefHeight(cardH);
                card.getChildren().stream()
                        .filter(c -> c instanceof ImageView)
                        .forEach(iv -> {
                            ((ImageView) iv).setFitWidth(cardW - 8);
                            ((ImageView) iv).setFitHeight(cardH - 40);
                        });
            }
        }
    }

    // =========================================================================
    // Grid mode (column count)
    // =========================================================================

    /**
     * Controls FlowPane wrap width to simulate a fixed column count.
     * Cards are ~160 px wide with a 16 px gap.
     */
    private void applyGridMode(int modeIndex) {
        // wrap = columns * (cardWidth + hgap)
        double gap      = 16;
        double baseCard = 160 * zoomLevel;
        int    cols     = modeIndex + 1;   // 0→1, 1→2, 2→3, 3→4
        pageGrid.setPrefWrapLength(cols * (baseCard + gap));
    }

    // =========================================================================
    // Export
    // =========================================================================

    @FXML
    private void onExport(ActionEvent e) {
        if (documents.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Export",
                    "There are no documents to export yet.");
            return;
        }
        String mode = exportModeComboBox.getValue();
        stExportLabel.setText("Export: running…");

        // TODO: ExportService.export(documents, mode, boxId)
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Export started:\n  Mode:  " + mode
                        + "\n  Docs:  " + documents.size()
                        + "\n  Pages: " + totalPageCount());

        stExportLabel.setText("Export: done");
    }

    // =========================================================================
    // API count
    // =========================================================================

    @FXML
    private void onRefreshApiCount(ActionEvent e) {
        // TODO: ApiUsageService.getRemainingCount() and update the label
        apiCountLabel.setText("API calls: — (not connected)");
    }

    // =========================================================================
    // Undo
    // =========================================================================

    @FXML
    private void onUndo(ActionEvent e) {
        if (!undoStack.isEmpty()) {
            undoStack.pop().run();
        }
    }

    private void pushUndo(Runnable reversal) {
        undoStack.push(reversal);
        // Cap depth to avoid unbounded growth
        while (undoStack.size() > UNDO_MAX) {
            undoStack.pollLast();
        }
    }

    // =========================================================================
    // Logout
    // =========================================================================

    @FXML
    private void onLogout(ActionEvent e) {
        // TODO: navigate back to the login / launcher screen
        showAlert(Alert.AlertType.INFORMATION, "Log out", "Logging out…");
    }

    // =========================================================================
    // Document tree selection
    // =========================================================================

    private void onTreeSelectionChanged(TreeItem<String> item) {
        if (item == null || item.getParent() == null) return;

        TreeItem<String> treeRoot  = documentTreeView.getRoot();
        boolean isDocNode  = item.getParent() == treeRoot;
        boolean isPageNode = !isDocNode
                && item.getParent().getParent() == treeRoot;

        if (isDocNode) {
            documents.stream()
                    .filter(d -> d.getName().equals(item.getValue()))
                    .findFirst()
                    .ifPresent(d -> {
                        selectedDocument = d;
                        if (!d.getPages().isEmpty())
                            selectPage(d, d.getPages().get(0));
                    });
        } else if (isPageNode) {
            String docName  = item.getParent().getValue();
            String pageName = item.getValue();
            documents.stream()
                    .filter(d -> d.getName().equals(docName))
                    .findFirst()
                    .ifPresent(d -> d.getPages().stream()
                            .filter(p -> p.getName().equals(pageName))
                            .findFirst()
                            .ifPresent(p -> selectPage(d, p)));
        }

        highlightSelectedCard();
        updatePageInfoLabel();
    }

    // =========================================================================
    // Page grid (FlowPane)
    // =========================================================================

    private void rebuildPageGrid() {
        pageGrid.getChildren().clear();
        for (Document doc : documents) {
            for (ScanPage page : doc.getPages()) {
                pageGrid.getChildren().add(buildPageCard(doc, page));
            }
        }
        highlightSelectedCard();
        updatePageInfoLabel();
        emptyStateLabel.setVisible(totalPageCount() == 0);
    }

    private VBox buildPageCard(Document doc, ScanPage page) {
        double cw = cardWidth();
        double ch = cardHeight();

        // Thumbnail — replace placeholder with real image loading
        ImageView thumb = new ImageView();
        // TODO: thumb.setImage(new Image(page.getThumbnailPath()));
        thumb.setFitWidth(cw - 8);
        thumb.setFitHeight(ch - 44);
        thumb.setPreserveRatio(true);

        Label nameLbl = new Label(page.getName());
        nameLbl.getStyleClass().add("lbl");
        nameLbl.setMaxWidth(cw - 8);

        Label docLbl = new Label(doc.getName());
        docLbl.getStyleClass().add("lbl");
        docLbl.setMaxWidth(cw - 8);
        docLbl.setStyle("-fx-font-size:9;");

        VBox card = new VBox(4, thumb, nameLbl, docLbl);
        card.setPrefWidth(cw);
        card.setPrefHeight(ch);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("card", "card-bg", "shadow");
        card.setPadding(new Insets(4));
        card.setRotate(page.getRotation());
        card.setUserData(page);    // used for selection highlighting

        card.setOnMouseClicked(ev -> {
            selectPage(doc, page);
            highlightSelectedCard();
            updatePageInfoLabel();
        });

        return card;
    }

    private void highlightSelectedCard() {
        for (Node node : pageGrid.getChildren()) {
            if (node instanceof VBox card) {
                boolean sel = card.getUserData() == selectedPage;
                card.setStyle(sel ? "-fx-border-color:#4a9eff; -fx-border-width:2; -fx-border-radius:4;" : "");
            }
        }
    }

    // =========================================================================
    // Document tree (left sidebar)
    // =========================================================================

    private void refreshTree() {
        TreeItem<String> root = documentTreeView.getRoot();
        root.getChildren().clear();

        for (Document doc : documents) {
            TreeItem<String> docItem = new TreeItem<>(doc.getName());
            docItem.setExpanded(true);
            for (ScanPage page : doc.getPages()) {
                docItem.getChildren().add(new TreeItem<>(page.getName()));
            }
            root.getChildren().add(docItem);
        }

        treeStatsLabel.setText(documents.size() + " docs · " + totalPageCount() + " pages");
    }

    // =========================================================================
    // Status bar + page label
    // =========================================================================

    private void refreshStatusBar() {
        stDocsLabel.setText("Docs: " + documents.size());
        stPagesLabel.setText("Pages: " + totalPageCount());
        stCurrentLabel.setText(selectedDocument != null ? selectedDocument.getName() : "–");
    }

    private void updatePageInfoLabel() {
        List<ScanPage> all = allPages();
        if (all.isEmpty()) {
            pageInfoLabel.setText("0 / 0");
            currentPageIndex = -1;
            return;
        }
        currentPageIndex = (selectedPage != null) ? all.indexOf(selectedPage) : 0;
        pageInfoLabel.setText((currentPageIndex + 1) + " / " + all.size());
    }

    // =========================================================================
    // Misc helpers
    // =========================================================================

    /** Rebuilds the grid, tree, and status bar in one call. */
    private void rebuild() {
        rebuildPageGrid();
        refreshTree();
        refreshStatusBar();
    }

    private void selectPage(Document doc, ScanPage page) {
        selectedDocument = doc;
        selectedPage     = page;
        currentPageIndex = allPages().indexOf(page);
        stCurrentLabel.setText(doc.getName() + "  ·  " + page.getName());
    }

    private List<ScanPage> allPages() {
        List<ScanPage> all = new ArrayList<>();
        for (Document doc : documents) all.addAll(doc.getPages());
        return all;
    }

    private int totalPageCount() {
        return documents.stream().mapToInt(d -> d.getPages().size()).sum();
    }

    private double cardWidth()  { return 160 * zoomLevel; }
    private double cardHeight() { return 220 * zoomLevel; }

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
        fade.setOnFinished(ev -> flashOverlay.setVisible(false));
        fade.play();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // =========================================================================
    // Inner model classes
    // Replace these with your actual domain model package once it exists.
    // =========================================================================

    /** Represents a single logical document made up of zero or more pages. */
    public static class Document {
        private String name;
        private final List<ScanPage> pages = new ArrayList<>();

        public Document(String name)          { this.name = name; }
        public String getName()               { return name; }
        public void   setName(String name)    { this.name = name; }
        public List<ScanPage> getPages()      { return pages; }
    }

    /** Represents a single scanned page (image + rotation metadata). */
    public static class ScanPage {
        private String name;
        private int    rotation;
        // TODO: add String filePath / Image thumbnail fields

        public ScanPage(String name, int rotation) {
            this.name     = name;
            this.rotation = rotation;
        }
        public String getName()              { return name; }
        public void   setName(String n)      { this.name = n; }
        public int    getRotation()          { return rotation; }
        public void   setRotation(int r)     { this.rotation = r; }
    }
}