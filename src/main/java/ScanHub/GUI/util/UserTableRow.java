package ScanHub.GUI.util;

import ScanHub.BE.User;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.BiConsumer;

/**
 * Utility class for creating styled JavaFX rows representing a {@link User}.
 * <p>
 * Each row is an HBox containing user information such as username,
 * and role along with visual elements like avatar (name initials) and separators.
 * <p>
 * Optionally supports a click handler via {@link BiConsumer} to define custom
 * behavior when a row is selected.
 */
public class UserTableRow {

    /**
     *
     * @param user
     * @return
     */
    public static HBox addRow(User user) {
        return addRow(user, null);
    }

    /**
     *
     * @param user
     * @param onSelect
     * @return
     */
    public static HBox addRow(User user, BiConsumer<User, HBox> onSelect) {

        // Avatar
        Label avatarLabel = new Label("JD");
        avatarLabel.getStyleClass().add("lbl");
        avatarLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        avatarLabel.setPrefHeight(30);
        avatarLabel.setPrefWidth(30);
        avatarLabel.setAlignment(Pos.CENTER);

        Pane avatar = new Pane(avatarLabel);
        avatar.getStyleClass().add("avatar-initial");
        avatar.setPrefHeight(30);
        avatar.setPrefWidth(30);
        avatar.setMaxHeight(Pane.USE_PREF_SIZE);
        avatar.setMaxWidth(Pane.USE_PREF_SIZE);
        avatar.setMinHeight(Pane.USE_PREF_SIZE);
        avatar.setMinWidth(Pane.USE_PREF_SIZE);
        HBox.setHgrow(avatar, Priority.NEVER);

        Region avatarSpacing = new Region();
        avatarSpacing.setPrefWidth(9);
        avatarSpacing.setPrefHeight(200);
        HBox.setHgrow(avatarSpacing, Priority.NEVER);

        Label nameLabel = new Label("John Doe");
        nameLabel.getStyleClass().add("lbl");

        HBox nameBox = new HBox(avatar, avatarSpacing, nameLabel);
        nameBox.setAlignment(Pos.CENTER);
        nameBox.setPrefWidth(210);
        nameBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // Separator 1
        Separator sep1 = new Separator(Orientation.VERTICAL);
        sep1.setPrefHeight(30);
        sep1.setMaxHeight(Separator.USE_PREF_SIZE);

        // Username
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().add("lbl");
        usernameLabel.setAlignment(Pos.CENTER);
        usernameLabel.setPrefWidth(210);
        usernameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(usernameLabel, Priority.ALWAYS);

        // Separator 2
        Separator sep2 = new Separator(Orientation.VERTICAL);
        sep2.setPrefHeight(30);
        sep2.setMaxHeight(Separator.USE_PREF_SIZE);

        // Email
        Label emailLabel = new Label("johndoe123@email.com");
        emailLabel.getStyleClass().add("lbl");
        emailLabel.setAlignment(Pos.CENTER);
        emailLabel.setPrefWidth(210);
        emailLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(emailLabel, Priority.ALWAYS);

        // Separator 3
        Separator sep3 = new Separator(Orientation.VERTICAL);
        sep3.setPrefHeight(30);
        sep3.setMaxHeight(Separator.USE_PREF_SIZE);

        // Role chip
        Label roleLabel = new Label(user.getRole().toString());
        roleLabel.getStyleClass().add("lbl");

        HBox chip = new HBox(roleLabel);
        chip.getStyleClass().add("chip");
        chip.setAlignment(Pos.CENTER);
        chip.setPrefHeight(27);
        chip.setPrefWidth(57);

        HBox chipColor = new HBox(chip);
        chipColor.getStyleClass().add("chip-color");
        chipColor.setAlignment(Pos.CENTER_RIGHT);
        chipColor.setPrefHeight(27);
        chipColor.setPrefWidth(60);
        chipColor.setMaxHeight(HBox.USE_PREF_SIZE);
        chipColor.setMaxWidth(HBox.USE_PREF_SIZE);
        chipColor.setMinHeight(HBox.USE_PREF_SIZE);
        chipColor.setMinWidth(HBox.USE_PREF_SIZE);

        HBox roleBox = new HBox(chipColor);
        roleBox.setAlignment(Pos.CENTER);
        roleBox.setPrefWidth(210);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(roleBox, Priority.ALWAYS);

        // Main row
        HBox row = new HBox(nameBox, sep1, usernameLabel, sep2, emailLabel, sep3, roleBox);
        row.getStyleClass().add("box-card");
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(45);
        row.setPrefWidth(200);
        row.setMaxWidth(Double.MAX_VALUE);

        if (onSelect != null) {
            row.setOnMouseClicked(e -> onSelect.accept(user, row));
        }

        return row;
    }
}
