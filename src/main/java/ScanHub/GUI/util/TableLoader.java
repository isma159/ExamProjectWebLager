package ScanHub.GUI.util;

import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;

public class TableLoader {

    public static void loadTable(
            VBox tableBox,
            Pagination pagination,
            int TOTAL_TABLE_SIZE,
            List<?> items,
            Function<Object, HBox> rowFactory
    ) {
        tableBox.getChildren().clear();
        pagination.setPageCount(Math.ceilDiv(items.size(), TOTAL_TABLE_SIZE));

        int start = pagination.getCurrentPageIndex() * TOTAL_TABLE_SIZE;
        int end = Math.min(start + TOTAL_TABLE_SIZE, items.size());

        for (Object item : items.subList(start, end)) {
            HBox row = rowFactory.apply(item);
            row.setFocusTraversable(true);
            row.focusedProperty().addListener((obs, old, focused) -> {
                if (focused) row.requestFocus();
            });
            row.setUserData(item);
            tableBox.getChildren().add(row);
        }
    }
}
