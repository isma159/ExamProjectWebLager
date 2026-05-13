package ScanHub.GUI.controllers;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.interfaces.TreeNode;
import ScanHub.BLL.SessionManager;
import ScanHub.GUI.util.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class UserController implements Initializable, IViewController {

    @FXML private TreeView<TreeNode> boxTreeView;
    @FXML private Label lblUsername, lblRole;
    @FXML private ToggleSwitch darkMode;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private final SessionManager sessionManager = SessionManager.getInstance();

    private ObservableList<Document> documents = FXCollections.observableArrayList();
    private Document selectedDocument = null;
    private File selectedPage = null;
    private int currentPageIndex = -1;
    private boolean sessionActive = false;

    // Zoom Level stuff
    private double zoomLevel = 1.0; // default
    private static double ZOOM_STEP = 0.15;
    private static double ZOOM_MIN = 0.40;
    private static double ZOOM_MAX = 3.00;

    public UserController() {}

    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initializeTreeView(boxTreeView);

        lblUsername.setText(sessionManager.getCurrentUser().getUsername());
        lblRole.setText("Role: " + sessionManager.getCurrentUser().getRole().toString());

        Box box = new Box(1, "Legal Documents", 1, LocalDateTime.now(), LocalDateTime.now());
        Document document = new Document(1, box.getBoxId(), LocalDateTime.now());
        File file = new File(1, document.getDocumentId(), 1, 1, 1, LocalDateTime.now());

        TreeItem<TreeNode> root = new TreeItem<>(box);
        TreeItem<TreeNode> parent = new TreeItem<>(document);
        TreeItem<TreeNode> child = new TreeItem<>(file);

        boxTreeView.setRoot(root);

        boxTreeView.getRoot().addEventHandler(TreeItem.childrenModificationEvent(), e -> {
            expandAll(boxTreeView.getRoot());
        });

        root.getChildren().add(parent);
        parent.getChildren().add(child);


    }

    public void expandAll(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandAll(child);
            }
        }
    }

    // Used to add icons and style classes to tree items, when they are added to the tree view
    private void initializeTreeView(TreeView<TreeNode> treeView) {

        treeView.setCellFactory(tv -> new TreeCell<>() {

            @Override
            protected void updateItem(TreeNode object, boolean empty) {
                super.updateItem(object, empty);
                if (empty || object == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    Label icon = new Label();
                    icon.getStyleClass().add("icon");

                    if (object instanceof Box box) {
                        icon.setText("\ue9d9");
                        icon.getStyleClass().add("tree-cell-box");
                        setText(box.getBoxName());
                    }
                    else if (object instanceof Document document) {
                        icon.setText("\ue963");
                        icon.getStyleClass().add("tree-cell-doc");
                        setText("Document #" + document.getDocumentId());
                    }
                    else if (object instanceof File file) {
                        icon.setText("\ue958");
                        icon.getStyleClass().add("tree-cell-file");
                        setText("File #" + file.getFileId());
                    }

                    setGraphic(icon);
                }
            }

        });
    }

    @FXML
    private void onDarkModeToggle() {
        ThemeManager.toggle(currentStage.getScene(), darkMode.isSelected());
    }

    @FXML
    private void onClickLogOut(ActionEvent actionEvent) {
        AlertHelper.showConfirmation("Log Out", "Are you sure you want to log out?", () -> {
            try {
                ViewHandler handler = ViewHandler.LOGIN;
                handler.reset();
                handler.show(modelFacade);
                sessionManager.logout();
                currentStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Logout Error", "Failed to log out. Please try again.");
            }
        });
    }
}
