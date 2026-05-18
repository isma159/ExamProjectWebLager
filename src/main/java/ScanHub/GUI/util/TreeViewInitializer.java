package ScanHub.GUI.util;

import ScanHub.BE.Profile;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.interfaces.CheckTreeNode;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.control.CheckTreeView;

public class TreeViewInitializer {

    public static void initUserFormTreeView(CheckTreeView<CheckTreeNode> checkTreeView) {

        checkTreeView.setCellFactory(tv -> new CheckBoxTreeCell<>() {

            @Override
            public void updateItem(CheckTreeNode item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("client-row", "profile-row", "bold-lbl");

                if (item == null || empty) {return;}

                Node checkBox = getGraphic();

                if (getTreeItem() != null && getTreeItem().getParent() == checkTreeView.getRoot()) {
                    getStyleClass().addAll("client-row", "bold-lbl");
                }
                else {
                    getStyleClass().add("profile-row");
                    setText(null);
                    Profile profile = (Profile) item;

                    HBox row = new HBox();
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(row, Priority.ALWAYS);

                    Label label = new Label(profile.getProfileName());

                    Region spacer = new Region();
                    spacer.setPrefWidth(15);

                    Region growingSpacer = new Region();
                    HBox.setHgrow(growingSpacer, Priority.ALWAYS);

                    String status = (profile.getStatus() == ProfileStatus.ACTIVE) ? "Active" : "Inactive";
                    String styleClass = (profile.getStatus() == ProfileStatus.ACTIVE) ? "chip-color-success" : "chip-color-error";
                    HBox chip = ChipMaker.createChip(status, styleClass);

                    row.getChildren().addAll(checkBox, spacer, label, growingSpacer, chip);
                    setGraphic(row);

                }
            }
        });
    }
}
