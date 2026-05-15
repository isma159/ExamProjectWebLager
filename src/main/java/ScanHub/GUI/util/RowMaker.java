package ScanHub.GUI.util;

// project imports
import ScanHub.BE.*;

// java imports
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

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
    private static final double AVATAR_SIZE = 30.0;
    private static final double SPACER_WIDTH = 9.0;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static HBox addUserRow(User user) {
        return addUserRow(user, null);
    }

    public static HBox addProfileRow(Profile profile) {
        return addProfileRow(profile, null);
    }

    public static HBox addUserRow(User user, BiConsumer<User, HBox> onSelect) {
        Label usernameLabel = createLabel(user.getUsername(), 210);
        HBox roleBox = centeredCol(ChipMaker.createChip(user.getRole().toString(), "chip-color"));

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(usernameLabel, roleBox);

        attachClickHandler(row, user, onSelect);
        return row;
    }

    public static HBox addClientRow (Client client, BiConsumer<Client, HBox> onSelect) {
        Label clientNameLabel = createLabel(client.getClientName(), 210);

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(clientNameLabel);

        attachClickHandler(row, client, onSelect);
        return row;
    }

    public static HBox addProfileRow(Profile profile, BiConsumer<Profile, HBox> onSelect) {
        HBox col1 = centeredCol(createLabel(profile.getProfileName()));
        HBox col2 = centeredCol(createLabel(profile.getSplitBehavior().toString() + "-SPLIT"));
        HBox col3 = centeredCol(createLabel(profile.getExportLabel()));
        HBox col4 = centeredCol(profileStatusChip(profile));

        HBox row = createBaseRow();
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(col1, col2, col3, col4);

        attachClickHandler(row, profile, onSelect);
        return row;
    }

    public static HBox addMetadataRow(BoxMetadata metadata, BiConsumer<BoxMetadata, HBox> onSelect) {
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

        attachClickHandler(row, metadata, onSelect);
        return row;
    }

    public static HBox addLogRow(Log log) {
        // Indicator dot
        Pane dot = new Pane();
        dot.setMaxWidth(Region.USE_PREF_SIZE);
        dot.setMaxHeight(Region.USE_PREF_SIZE);
        dot.setMinWidth(Region.USE_PREF_SIZE);
        dot.setMinHeight(Region.USE_PREF_SIZE);
        dot.setPrefSize(6.0, 6.0);
        dot.getStyleClass().add("avatar-" + log.getAction().toString().toLowerCase());

        HBox col1 = new HBox(dot);
        col1.setAlignment(Pos.CENTER);
        col1.setPrefSize(100.0, COL_PREF_HEIGHT);
        HBox.setHgrow(col1, Priority.NEVER);

        // Log ID
        HBox col2 = new HBox(createLabel("Log ID: " + log.getLogId()));
        col2.setAlignment(Pos.CENTER);
        col2.setPrefSize(100.0, COL_PREF_HEIGHT);
        HBox.setHgrow(col2, Priority.NEVER);

        // Description
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

    public static HBox addProfileRowToForm(Profile profile, User user, BiConsumer<Profile, Boolean> onCheckChanged) {
        CheckBox checkBox = new CheckBox();
        checkBox.setMnemonicParsing(false);

        if (user != null) {
            checkBox.setSelected(user.getProfiles().contains(profile));
        }

        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> onCheckChanged.accept(profile, newVal));

        return buildFormRow(checkBox, profile.getProfileName(), profileStatusChip(profile));
    }

    public static HBox addUserRowToForm(User user, Profile profile, BiConsumer<User, Boolean> onCheckChanged) {
        CheckBox checkBox = new CheckBox();
        checkBox.setMnemonicParsing(false);

        if (profile != null) {
            checkBox.setSelected(user.getProfiles().contains(profile));
        }

        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> onCheckChanged.accept(user, newVal));

        return buildFormRow(checkBox, user.getUsername(), ChipMaker.createChip(user.getRole().toString(), "chip-color"));
    }

    /** Base row shared by all non-form rows. */
    private static HBox createBaseRow() {
        HBox row = new HBox();
        row.getStyleClass().add("box-card");
        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinHeight(Region.USE_PREF_SIZE);
        row.setPrefHeight(ROW_HEIGHT);
        row.setPrefWidth(ROW_PREF_WIDTH);
        return row;
    }

    /**
     * Builds the checkbox form row shared by {@code addProfileRowToForm} and
     * {@code addUserRowToForm}, differing only in the display name and chip.
     */
    private static HBox buildFormRow(CheckBox checkBox, String displayName, HBox chip) {
        HBox chipHolder = new HBox(chip);
        chipHolder.setAlignment(Pos.CENTER);
        chipHolder.setMaxWidth(Region.USE_COMPUTED_SIZE);
        chipHolder.setMinWidth(Region.USE_COMPUTED_SIZE);
        chipHolder.setPrefSize(90.0, COL_PREF_HEIGHT);
        HBox.setHgrow(chipHolder, Priority.NEVER);

        HBox row = new HBox(
                fixedSpacer(),
                checkBox,
                fixedSpacer(),
                createAvatarPane(displayName),
                fixedSpacer(),
                createLabel(displayName),
                growingSpacer(),
                chipHolder,
                fixedSpacer()
        );

        row.setAlignment(Pos.CENTER);
        row.setMaxHeight(Region.USE_PREF_SIZE);
        row.setMinHeight(Region.USE_PREF_SIZE);
        row.setPrefHeight(ROW_HEIGHT);
        row.setPrefWidth(ROW_PREF_WIDTH);
        row.getStyleClass().add("box-card");
        return row;
    }

    /** Creates a centered, growing HBox column containing the given node. */
    private static HBox centeredCol(javafx.scene.Node node) {
        HBox col = new HBox(node);
        col.setAlignment(Pos.CENTER);
        col.setPrefSize(COL_PREF_WIDTH, COL_PREF_HEIGHT);
        HBox.setHgrow(col, Priority.ALWAYS);
        return col;
    }

    /** Column 1 variant: circular avatar + spacer + name label. */
    private static HBox createAvatarNameCol(String name) {
        HBox col = new HBox(
                createAvatarPane(name),
                fixedSpacer(),
                createLabel(name)
        );
        col.setAlignment(Pos.CENTER);
        col.setPrefSize(COL_PREF_WIDTH, COL_PREF_HEIGHT);
        HBox.setHgrow(col, Priority.ALWAYS);
        return col;
    }

    /** Creates a label with the "lbl" style class and optional preferred width. */
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

    /** Circular avatar pane showing the first character of {@code name}. */
    private static Pane createAvatarPane(String name) {
        Label initial = new Label(name.strip().substring(0, 1));
        initial.setAlignment(Pos.CENTER);
        initial.setPrefSize(AVATAR_SIZE, AVATAR_SIZE);
        initial.getStyleClass().add("lbl");
        initial.setTextFill(Paint.valueOf("WHITE"));

        Pane avatar = new Pane(initial);
        avatar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        avatar.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        avatar.setPrefSize(AVATAR_SIZE, AVATAR_SIZE);
        avatar.getStyleClass().add("avatar-initial");
        return avatar;
    }

    /** Fixed-width spacer that never grows. */
    private static Region fixedSpacer() {
        Region spacer = new Region();
        spacer.setMaxWidth(Region.USE_PREF_SIZE);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setPrefSize(RowMaker.SPACER_WIDTH, COL_PREF_HEIGHT);
        HBox.setHgrow(spacer, Priority.NEVER);
        return spacer;
    }

    /** Growing spacer that pushes subsequent nodes to the right. */
    private static Region growingSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /** Returns the appropriate status chip for a {@link Profile}. */
    private static HBox profileStatusChip(Profile profile) {
        return profile.getStatus() == ProfileStatus.ACTIVE
                ? ChipMaker.createChip("Active", "chip-color-success")
                : ChipMaker.createChip("Inactive", "chip-color-error");
    }

    /** Builds the human-readable description string for a log entry. */
    private static String buildLogDescription(Log log) {
        String ts = log.getTimestamp().format(DATETIME_FORMATTER);

        if (log.getAction() == LogAction.LOGIN) {
            return log.getUser().getUsername() + " logged in at " + ts;
        }

        String verb = switch (log.getAction()) {
            case EXPORT -> "Exported";
            case CREATE -> "Created";
            case DELETE -> "Deleted";
            case SCAN -> "Scanned";
            default -> log.getAction().toString();
        };

        return verb + " " + log.getEntityType().toString().toLowerCase()
                + " " + log.getEntityId() + " on " + ts;
    }

    /** Attaches a mouse-click handler to {@code row} if {@code handler} is non-null. */
    private static <T> void attachClickHandler(HBox row, T item, BiConsumer<T, HBox> handler) {
        if (handler != null) {
            row.setOnMouseClicked(e -> handler.accept(item, row));
        }
    }
}