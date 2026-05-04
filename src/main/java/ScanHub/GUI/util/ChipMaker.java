package ScanHub.GUI.util;

// java imports
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ChipMaker {

    public static HBox createChip(String text, String colorStyleClass) {
        // Outer HBox
        HBox outerHBox = new HBox();
        outerHBox.setAlignment(Pos.CENTER_RIGHT);
        outerHBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        outerHBox.setMinHeight(Double.NEGATIVE_INFINITY);
        outerHBox.setPrefHeight(30.0);
        outerHBox.setPrefWidth(75.0);
        outerHBox.getStyleClass().add(colorStyleClass); // e.g. "chip-color-success"

        // Left spacer region
        Region leftSpacer = new Region();
        leftSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        leftSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        leftSpacer.setPrefHeight(30.0);
        leftSpacer.setPrefWidth(6.0);

        // Inner HBox
        HBox innerHBox = new HBox();
        innerHBox.setAlignment(Pos.CENTER);
        innerHBox.setPrefHeight(30.0);
        innerHBox.setPrefWidth(69.0);
        innerHBox.getStyleClass().add("chip");
        HBox.setHgrow(innerHBox, Priority.ALWAYS);

        // Inner left spacer
        Region innerLeftSpacer = new Region();
        innerLeftSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        innerLeftSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        innerLeftSpacer.setPrefHeight(30.0);
        innerLeftSpacer.setPrefWidth(6.0);
        HBox.setHgrow(innerLeftSpacer, Priority.NEVER);

        // Label
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("lbl");

        // Inner right spacer
        Region innerRightSpacer = new Region();
        innerRightSpacer.setLayoutX(10.0);
        innerRightSpacer.setLayoutY(10.0);
        innerRightSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        innerRightSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        innerRightSpacer.setPrefHeight(30.0);
        innerRightSpacer.setPrefWidth(6.0);
        HBox.setHgrow(innerRightSpacer, Priority.NEVER);

        innerHBox.getChildren().addAll(innerLeftSpacer, label, innerRightSpacer);
        outerHBox.getChildren().addAll(leftSpacer, innerHBox);

        return outerHBox;
    }

}
