package ScanHub.GUI.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ChipMaker {

    private static final double HEIGHT = 30;
    private static final double WIDTH = 75;
    private static final double SPACER = 6;

    public static HBox createChip(String text, String colorStyleClass) {

        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("lbl");

        HBox inner = new HBox(label);
        inner.setAlignment(Pos.CENTER);
        inner.setPrefSize(WIDTH - SPACER, HEIGHT);
        inner.setPadding(new Insets(0, SPACER, 0, SPACER));
        inner.getStyleClass().add("chip");
        HBox.setHgrow(inner, Priority.ALWAYS);

        Region leftSpacer = new Region();
        leftSpacer.setPrefWidth(SPACER);

        HBox outer = new HBox(leftSpacer, inner);
        outer.setAlignment(Pos.CENTER_RIGHT);
        outer.setPrefSize(WIDTH, HEIGHT);
        outer.setMaxWidth(Region.USE_PREF_SIZE);
        outer.setMaxHeight(Region.USE_PREF_SIZE);
        outer.setMinWidth(Region.USE_PREF_SIZE);
        outer.setMinHeight(Region.USE_PREF_SIZE);
        outer.getStyleClass().add(colorStyleClass);

        return outer;
    }
}