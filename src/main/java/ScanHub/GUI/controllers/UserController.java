package ScanHub.GUI.controllers;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.interfaces.TreeNode;
import ScanHub.GUI.facade.ModelFacade;
import com.sun.source.tree.Tree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import javax.print.Doc;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private TreeView<TreeNode> boxTreeView;

    private Stage currentStage;
    private ModelFacade modelFacade;

    public UserController() {}

    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        TreeItem<TreeNode> root = new TreeItem<>(new Box(1, "Legal_Documents", 2, LocalDateTime.now(), LocalDateTime.now()));

        Box box = (Box) root.getValue();

        TreeItem<TreeNode> parent = new TreeItem<>(new Document(1, box.getBoxId(), LocalDateTime.now()));

        Document document = (Document) parent.getValue();

        TreeItem<TreeNode> child = new TreeItem<>(new File(1, document.getDocumentId(), 1, 1, 1, LocalDateTime.now()));
        root.setExpanded(true);

        boxTreeView.setRoot(root);

        boxTreeView.getRoot().getChildren().add(parent);

        parent.getChildren().add(child);


    }


}
