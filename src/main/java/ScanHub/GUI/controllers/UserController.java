package ScanHub.GUI.controllers;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.interfaces.TreeNode;
import ScanHub.BLL.SessionManager;
import ScanHub.BLL.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IViewController;
import com.sun.source.tree.Tree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;

import javax.print.Doc;
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

    public UserController() {}

    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    public void expandAll(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandAll(child);
            }
        }
    }

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


}
