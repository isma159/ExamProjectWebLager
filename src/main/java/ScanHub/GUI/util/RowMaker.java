package ScanHub.GUI.util;

// project imports
import ScanHub.BE.*;

// java imports
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Utility class for creating styled JavaFX rows representing domain objects.
 * <p>
 * Each row is an {@link HBox} containing relevant fields along with visual
 * elements like avatars (name initials) and status chips.
 * <p>
 * Optionally supports a click handler via {@link BiConsumer} to define custom
 * behavior when a row is selected.
 */
public class RowMaker {

    private static final double ROW_HEIGHT = 45.0;
    private static final double ROW_PREF_WIDTH = 200.0;
    private static final double COL_PREF_HEIGHT = 100.0;
    private static final double COL_PREF_WIDTH = 200.0;
    private static final double SPACER_WIDTH = 9.0;
    private static final double ACTIONS_WIDTH = 160.0; // edit + delete + gap

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ----- Dashboard overloads (no action buttons) -----

    public static HBox addUserRow(User user) {
        return addUserRow(user, null, null, null);
    }

    public static HBox addProfileRow(Profile profile) {
        return addProfileRow(profile, null, null, null);
    }

    // ----- Full overloads (select + edit + delete) -----

    public static HBox addUserRow(User user, BiConsumer<User, HBox> onSelect, Consumer<User> onEdit, Consumer<User> onDelete) {
        Label usernameLabel = createLabel(user.getUsername(), 210);
        HBox roleBox = centeredCol(ChipMaker.createChip(user.getRole().toString(), "chip-color"));

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(usernameLabel, roleBox);

        appendActionButtons(row, user, onEdit, onDelete);
        attachClickHandler(row, user, onSelect);
        return row;
    }

    public static HBox addProfileRow(Profile profile, BiConsumer<Profile, HBox> onSelect, Consumer<Profile> onEdit, Consumer<Profile> onDelete) {
        HBox col1 = centeredCol(createLabel(profile.getProfileName()));
        HBox col2 = centeredCol(createLabel(profile.getExportLabel()));
        HBox col3 = centeredCol(profileStatusChip(profile));

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(col1, col2, col3);

        appendActionButtons(row, profile, onEdit, onDelete);
        attachClickHandler(row, profile, onSelect);
        return row;
    }

    public static HBox addClientRow(Client client, BiConsumer<Client, HBox> onSelect, Consumer<Client> onEdit, Consumer<Client> onDelete) {
        Label clientNameLabel = createLabel(client.getClientName(), 210);

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(clientNameLabel);

        appendActionButtons(row, client, onEdit, onDelete);
        attachClickHandler(row, client, onSelect);
        return row;
    }

    // -----  -----

    public static HBox addMetadataRow(BoxMetadata metadata) {
        HBox col1 = centeredCol(createLabel("Box #" + metadata.getBoxId()));
        HBox col2 = centeredCol(createLabel(metadata.getProfileName()));
        HBox col3 = centeredCol(createLabel(metadata.getBoxName()));
        HBox col4 = centeredCol(createLabel("Docs: " + metadata.getDocumentCount()));
        HBox col5 = centeredCol(createLabel("Files: " + metadata.getFileCount()));
        HBox col6 = centeredCol(createLabel(metadata.getBoxCreatedAt().format(DATETIME_FORMATTER)));

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(col1, col2, col3, col4, col5, col6);

        return row;
    }

    public static HBox addLogRow(Log log) {
        // indicator dot
        Pane dot = new Pane();
        dot.setMaxWidth(Region.USE_PREF_SIZE);
        dot.setMaxHeight(Region.USE_PREF_SIZE);
        dot.setMinWidth(Region.USE_PREF_SIZE);
        dot.setMinHeight(Region.USE_PREF_SIZE);
        dot.setPrefSize(6.0, 6.0);

        if (log.getAction() == LogAction.CREATE) {dot.getStyleClass().add("avatar-green");}
        else if (log.getAction() == LogAction.DELETE) {dot.getStyleClass().add("avatar-orange");}
        else if (log.getAction() == LogAction.EXPORT) {dot.getStyleClass().add("avatar-purple");}
        else if (log.getAction() == LogAction.UPDATE) {dot.getStyleClass().add("avatar-yellow");}
        else if (log.getAction() == LogAction.ERROR) {dot.getStyleClass().add("avatar-red");}
        else {dot.getStyleClass().add("avatar-initial");}

        HBox col1 = new HBox(dot);
        col1.setAlignment(Pos.CENTER);
        col1.setPrefSize(100.0, COL_PREF_HEIGHT);
        HBox.setHgrow(col1, Priority.NEVER);

        // Log ID
        HBox col2 = new HBox(createLabel("Log ID: " + log.getLogId()));
        col2.setAlignment(Pos.CENTER);
        col2.setPrefSize(100.0, COL_PREF_HEIGHT);
        HBox.setHgrow(col2, Priority.NEVER);

        // description
        HBox col3 = new HBox(createLabel(buildLogDescription(log)));
        col3.setAlignment(Pos.CENTER_LEFT);
        col3.setPrefSize(COL_PREF_WIDTH, COL_PREF_HEIGHT);
        HBox.setHgrow(col3, Priority.ALWAYS);

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(col1, col2, col3);
        return row;
    }

    // ----- Action button injection -----

    /**
     * Appends an action button pane (edit + delete) to the row when at least
     * one handler is non-null. The pane is always managed (takes up space) so
     * the row layout doesn't shift when it appears, but starts invisible and
     * is revealed on hover or when the row carries the "row-selected" css.
     */
    private static <T> void appendActionButtons(HBox row, T item, Consumer<T> onEdit, Consumer<T> onDelete) {
        if (onEdit == null && onDelete == null) return;

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMinWidth(ACTIONS_WIDTH);
        actions.setMaxWidth(ACTIONS_WIDTH);
        actions.setPrefWidth(ACTIONS_WIDTH);
        actions.setVisible(false);

        if (onEdit != null) {
            Button editBtn = new Button("\uD83D\uDD89 Edit"); // ✎
            editBtn.getStyleClass().add("primary-btn");
            editBtn.setOnAction(e -> {
                e.consume(); // don't bubble to the row's mouse-click handler
                onEdit.accept(item);
            });
            editBtn.setTooltip(new Tooltip("Edit (Ctrl + E)"));
            actions.getChildren().add(editBtn);
        }

        if (onDelete != null) {
            Button deleteBtn = new Button("\uD83D\uDDD1 Delete");
            deleteBtn.getStyleClass().add("destructive-btn");
            deleteBtn.setOnAction(e -> {
                e.consume();
                onDelete.accept(item);
            });
            deleteBtn.setTooltip(new Tooltip("Delete (Del)"));
            actions.getChildren().add(deleteBtn);
        }

        // reveal on hover
        row.hoverProperty().addListener((obs, wasHovered, isHovered) ->
                actions.setVisible(isHovered || row.getStyleClass().contains("row-selected")));

        // reveal when selected
        row.getStyleClass().addListener((javafx.collections.ListChangeListener<String>) change ->
                actions.setVisible(row.isHover() || row.getStyleClass().contains("row-selected")));

        row.getChildren().addAll(fixedSpacer(), actions, fixedSpacer());
    }

    // ----- Helpers -----

    private static HBox createBaseRow() {
        HBox row = new HBox();
        row.getStyleClass().add("box-card");
        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinHeight(Region.USE_PREF_SIZE);
        row.setPrefHeight(ROW_HEIGHT);
        row.setPrefWidth(ROW_PREF_WIDTH);
        return row;
    }

    private static HBox centeredCol(javafx.scene.Node node) {
        HBox col = new HBox(node);
        col.setAlignment(Pos.CENTER);
        col.setPrefSize(COL_PREF_WIDTH, COL_PREF_HEIGHT);
        HBox.setHgrow(col, Priority.ALWAYS);
        return col;
    }

    private static Label createLabel(String text, double prefWidth) {
        Label lbl = createLabel(text);
        lbl.setAlignment(Pos.CENTER);
        lbl.setPrefWidth(prefWidth);
        lbl.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lbl, Priority.ALWAYS);
        return lbl;
    }

    private static Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("lbl");
        HBox.setHgrow(lbl, Priority.ALWAYS);
        return lbl;
    }

    private static Region fixedSpacer() {
        Region spacer = new Region();
        spacer.setMaxWidth(Region.USE_PREF_SIZE);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setPrefSize(SPACER_WIDTH, COL_PREF_HEIGHT);
        HBox.setHgrow(spacer, Priority.NEVER);
        return spacer;
    }

    private static HBox profileStatusChip(Profile profile) {
        return profile.getStatus() == ProfileStatus.ACTIVE
                ? ChipMaker.createChip("Active",   "chip-color-success")
                : ChipMaker.createChip("Inactive", "chip-color-error");
    }

    private static String buildLogDescription(Log log) {
        String ts = log.getTimestamp().format(DATETIME_FORMATTER);
        if (log.getAction() == LogAction.LOGIN)
            return log.getUser().getUsername() + " " + log.getAction().getVerb() + " at " + ts;
        return log.getUser().getUsername() + " " + log.getAction().getVerb()
                + " " + log.getEntityType().getLabel()
                + " " + log.getEntityName() + " on " + ts;
    }

    private static <T> void attachClickHandler(HBox row, T item, BiConsumer<T, HBox> handler) {
        if (handler != null) row.setOnMouseClicked(e -> handler.accept(item, row));
    }
}
