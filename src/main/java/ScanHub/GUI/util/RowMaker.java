package ScanHub.GUI.util;

import ScanHub.BE.Profile;
import ScanHub.BE.ProfileStatus;
import ScanHub.BE.Role;
import ScanHub.BE.User;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

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
public class RowMaker {

    /**
     *
     * @param user
     * @return
     */
    public static HBox addUserRow(User user) {
        return addUserRow(user, null);
    }

    public static HBox addProfileRow(Profile profile) {
        return addProfileRow(profile, null);
    }

    /**
     *
     * @param user
     * @param onSelect
     * @return
     */
    public static HBox addUserRow(User user, BiConsumer<User, HBox> onSelect) {

        // Username
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().add("lbl");
        usernameLabel.setAlignment(Pos.CENTER);
        usernameLabel.setPrefWidth(210);
        usernameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(usernameLabel, Priority.ALWAYS);

        /* Separator 2
        Separator sep2 = new Separator(Orientation.VERTICAL);
        sep2.setPrefHeight(30);
        sep2.setMaxHeight(Separator.USE_PREF_SIZE);*/

        /* Separator 3
        Separator sep3 = new Separator(Orientation.VERTICAL);
        sep3.setPrefHeight(30);
        sep3.setMaxHeight(Separator.USE_PREF_SIZE);*/

        // Role chip
        HBox chip = ChipMaker.createChip(user.getRole().toString(), "chip-color");

        HBox roleBox = new HBox(chip);
        roleBox.setAlignment(Pos.CENTER);
        roleBox.setPrefWidth(210);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(roleBox, Priority.ALWAYS);

        // Main row
        HBox row = new HBox(usernameLabel, roleBox);
        row.getStyleClass().add("box-card");
        row.getStyleClass().add("user-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(45);
        row.setPrefWidth(200);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinHeight(Region.USE_PREF_SIZE);

        if (onSelect != null) {
            row.setOnMouseClicked(e -> onSelect.accept(user, row));
        }

        return row;
    }

    public static HBox addProfileRow(Profile profile, BiConsumer<Profile, HBox> onSelect) {

        // Outer HBox
        HBox outerHBox = new HBox();
        outerHBox.setAlignment(Pos.CENTER_LEFT);
        outerHBox.setMaxWidth(Double.MAX_VALUE);
        outerHBox.setMinHeight(Region.USE_PREF_SIZE);
        outerHBox.setPrefHeight(45.0);
        outerHBox.setPrefWidth(200.0);
        outerHBox.getStyleClass().add("box-card");
        outerHBox.getStyleClass().add("user-row");

        // --- Column 1: Avatar + Name ---
        HBox col1 = new HBox();
        col1.setAlignment(Pos.CENTER);
        col1.setPrefHeight(100.0);
        col1.setPrefWidth(200.0);
        HBox.setHgrow(col1, Priority.ALWAYS);

        Pane avatar = new Pane();
        avatar.setMaxHeight(Double.NEGATIVE_INFINITY);
        avatar.setMaxWidth(Double.NEGATIVE_INFINITY);
        avatar.setMinHeight(Double.NEGATIVE_INFINITY);
        avatar.setMinWidth(Double.NEGATIVE_INFINITY);
        avatar.setPrefHeight(30.0);
        avatar.setPrefWidth(30.0);
        avatar.getStyleClass().add("avatar-initial");

        Label avatarLabel = new Label(profile.getProfileName().strip().substring(0, 1));
        avatarLabel.setAlignment(Pos.CENTER);
        avatarLabel.setPrefHeight(30.0);
        avatarLabel.setPrefWidth(30.0);
        avatarLabel.getStyleClass().add("lbl");
        avatarLabel.setTextFill(Paint.valueOf("WHITE"));
        avatar.getChildren().add(avatarLabel);

        Region col1Spacer = new Region();
        col1Spacer.setPrefHeight(200.0);
        col1Spacer.setPrefWidth(9.0);
        HBox.setHgrow(col1Spacer, Priority.NEVER);

        Label nameLabel = new Label(profile.getProfileName());
        nameLabel.getStyleClass().add("lbl");

        col1.getChildren().addAll(avatar, col1Spacer, nameLabel);

        // --- Column 2: Split behaviour
        HBox col2 = new HBox();
        col2.setAlignment(Pos.CENTER);
        col2.setLayoutX(10.0);
        col2.setLayoutY(10.0);
        col2.setPrefHeight(100.0);
        col2.setPrefWidth(200.0);
        HBox.setHgrow(col2, Priority.ALWAYS);

        Label barcodeLabel = new Label(profile.getSplitBehavior().toString() + "-SPLIT");
        barcodeLabel.getStyleClass().add("lbl");
        col2.getChildren().add(barcodeLabel);

        // --- Column 4: Export label
        HBox col3 = new HBox();
        col3.setAlignment(Pos.CENTER);
        col3.setLayoutX(1608.0);
        col3.setLayoutY(10.0);
        col3.setPrefHeight(100.0);
        col3.setPrefWidth(200.0);
        HBox.setHgrow(col3, Priority.ALWAYS);

        Label boxIdLabel = new Label(profile.getExportLabel());
        boxIdLabel.getStyleClass().add("lbl");
        col3.getChildren().add(boxIdLabel);

        // --- Column 5: Chip ---
        HBox col4 = new HBox();
        col4.setAlignment(Pos.CENTER);
        col4.setLayoutX(1608.0);
        col4.setLayoutY(10.0);
        col4.setPrefHeight(100.0);
        col4.setPrefWidth(200.0);
        HBox.setHgrow(col4, Priority.ALWAYS);

        // Chip

        HBox chip = null;
        if (profile.getStatus() == ProfileStatus.Active) {
            chip = ChipMaker.createChip("Active", "chip-color-success");
        }
        else {
            chip = ChipMaker.createChip("Inactive", "chip-color-error");
        }

        col4.getChildren().add(chip);

        outerHBox.getChildren().addAll(col1, col2, col3, col4);

        if (onSelect != null) {
            outerHBox.setOnMouseClicked(e -> onSelect.accept(profile, outerHBox));
        }

        return outerHBox;
    }

    public static HBox addProfileRowToForm(Profile profile, User user, BiConsumer<Profile, Boolean> onCheckChangedProfile) {

        // Outer HBox
        HBox outerHBox = new HBox();
        outerHBox.setAlignment(Pos.CENTER);
        outerHBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        outerHBox.setMinHeight(Double.NEGATIVE_INFINITY);
        outerHBox.setPrefHeight(45.0);
        outerHBox.setPrefWidth(200.0);
        outerHBox.getStyleClass().add("box-card");

        // Left spacer
        Region leftSpacer = new Region();
        leftSpacer.setLayoutX(299.0);
        leftSpacer.setLayoutY(10.0);
        leftSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        leftSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        leftSpacer.setPrefHeight(200.0);
        leftSpacer.setPrefWidth(9.0);
        HBox.setHgrow(leftSpacer, Priority.NEVER);

        // Checkbox
        CheckBox checkBox = new CheckBox();
        checkBox.setMnemonicParsing(false);

        // Spacer between checkbox and avatar
        Region checkAvatarSpacer = new Region();
        checkAvatarSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        checkAvatarSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        checkAvatarSpacer.setPrefHeight(200.0);
        checkAvatarSpacer.setPrefWidth(9.0);
        HBox.setHgrow(checkAvatarSpacer, Priority.NEVER);

        // Avatar pane
        Pane avatar = new Pane();
        avatar.setMaxHeight(Double.NEGATIVE_INFINITY);
        avatar.setMaxWidth(Double.NEGATIVE_INFINITY);
        avatar.setMinHeight(Double.NEGATIVE_INFINITY);
        avatar.setMinWidth(Double.NEGATIVE_INFINITY);
        avatar.setPrefHeight(30.0);
        avatar.setPrefWidth(30.0);
        avatar.getStyleClass().add("avatar-initial");

        Label avatarLabel = new Label(profile.getProfileName().substring(0, 1));
        avatarLabel.setAlignment(Pos.CENTER);
        avatarLabel.setPrefHeight(30.0);
        avatarLabel.setPrefWidth(30.0);
        avatarLabel.getStyleClass().add("lbl");
        avatarLabel.setTextFill(Paint.valueOf("WHITE"));
        avatar.getChildren().add(avatarLabel);

        // Spacer between avatar and name label
        Region avatarLabelSpacer = new Region();
        avatarLabelSpacer.setLayoutX(206.0);
        avatarLabelSpacer.setLayoutY(10.0);
        avatarLabelSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        avatarLabelSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        avatarLabelSpacer.setPrefHeight(200.0);
        avatarLabelSpacer.setPrefWidth(9.0);
        HBox.setHgrow(avatarLabelSpacer, Priority.NEVER);

        // Name label
        Label nameLabel = new Label(profile.getProfileName());

        // Growing spacer (pushes chip to the right)
        Region growingSpacer = new Region();
        growingSpacer.setLayoutX(186.0);
        growingSpacer.setLayoutY(10.0);
        growingSpacer.setPrefHeight(200.0);
        growingSpacer.setPrefWidth(9.0);
        HBox.setHgrow(growingSpacer, Priority.ALWAYS);

        // Chip placeholder HBox
        HBox chipPlaceholder = new HBox();
        chipPlaceholder.setAlignment(Pos.CENTER);
        chipPlaceholder.setMaxWidth(Double.NEGATIVE_INFINITY);
        chipPlaceholder.setMinWidth(Double.NEGATIVE_INFINITY);
        chipPlaceholder.setPrefHeight(100.0);
        chipPlaceholder.setPrefWidth(90.0);
        HBox.setHgrow(chipPlaceholder, Priority.NEVER);

        HBox chip = null;
        if (profile.getStatus() == ProfileStatus.Active) {
            chip = ChipMaker.createChip("Active", "chip-color-success");
        }
        else {
            chip = ChipMaker.createChip("Inactive", "chip-color-error");
        }
        chipPlaceholder.getChildren().add(chip);

        // Right spacer
        Region rightSpacer = new Region();
        rightSpacer.setLayoutX(77.0);
        rightSpacer.setLayoutY(10.0);
        rightSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        rightSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        rightSpacer.setPrefHeight(200.0);
        rightSpacer.setPrefWidth(9.0);

        outerHBox.getChildren().addAll(
                leftSpacer,
                checkBox,
                checkAvatarSpacer,
                avatar,
                avatarLabelSpacer,
                nameLabel,
                growingSpacer,
                chipPlaceholder,
                rightSpacer
        );

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {

            onCheckChangedProfile.accept(profile, newValue);

        });

        if (user != null) {
            checkBox.setSelected(user.getProfiles().contains(profile));
            System.out.println(user.getProfiles());
        }

        return outerHBox;
    }

    public static HBox addUserRowToForm(User user, Profile profile, BiConsumer<User, Boolean> onCheckChangedUser) {

        // Outer HBox
        HBox outerHBox = new HBox();
        outerHBox.setAlignment(Pos.CENTER);
        outerHBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        outerHBox.setMinHeight(Double.NEGATIVE_INFINITY);
        outerHBox.setPrefHeight(45.0);
        outerHBox.setPrefWidth(200.0);
        outerHBox.getStyleClass().add("box-card");

        // Left spacer
        Region leftSpacer = new Region();
        leftSpacer.setLayoutX(299.0);
        leftSpacer.setLayoutY(10.0);
        leftSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        leftSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        leftSpacer.setPrefHeight(200.0);
        leftSpacer.setPrefWidth(9.0);
        HBox.setHgrow(leftSpacer, Priority.NEVER);

        // Checkbox
        CheckBox checkBox = new CheckBox();
        checkBox.setMnemonicParsing(false);

        // Spacer between checkbox and avatar
        Region checkAvatarSpacer = new Region();
        checkAvatarSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        checkAvatarSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        checkAvatarSpacer.setPrefHeight(200.0);
        checkAvatarSpacer.setPrefWidth(9.0);
        HBox.setHgrow(checkAvatarSpacer, Priority.NEVER);

        // Avatar pane
        Pane avatar = new Pane();
        avatar.setMaxHeight(Double.NEGATIVE_INFINITY);
        avatar.setMaxWidth(Double.NEGATIVE_INFINITY);
        avatar.setMinHeight(Double.NEGATIVE_INFINITY);
        avatar.setMinWidth(Double.NEGATIVE_INFINITY);
        avatar.setPrefHeight(30.0);
        avatar.setPrefWidth(30.0);
        avatar.getStyleClass().add("avatar-initial");

        Label avatarLabel = new Label(user.getUsername().substring(0, 1));
        avatarLabel.setAlignment(Pos.CENTER);
        avatarLabel.setPrefHeight(30.0);
        avatarLabel.setPrefWidth(30.0);
        avatarLabel.getStyleClass().add("lbl");
        avatarLabel.setTextFill(Paint.valueOf("WHITE"));
        avatar.getChildren().add(avatarLabel);

        // Spacer between avatar and name label
        Region avatarLabelSpacer = new Region();
        avatarLabelSpacer.setLayoutX(206.0);
        avatarLabelSpacer.setLayoutY(10.0);
        avatarLabelSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        avatarLabelSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        avatarLabelSpacer.setPrefHeight(200.0);
        avatarLabelSpacer.setPrefWidth(9.0);
        HBox.setHgrow(avatarLabelSpacer, Priority.NEVER);

        // Name label
        Label nameLabel = new Label(user.getUsername());

        // Growing spacer (pushes chip to the right)
        Region growingSpacer = new Region();
        growingSpacer.setLayoutX(186.0);
        growingSpacer.setLayoutY(10.0);
        growingSpacer.setPrefHeight(200.0);
        growingSpacer.setPrefWidth(9.0);
        HBox.setHgrow(growingSpacer, Priority.ALWAYS);

        // Chip placeholder HBox
        HBox chipPlaceholder = new HBox();
        chipPlaceholder.setAlignment(Pos.CENTER);
        chipPlaceholder.setMaxWidth(Double.NEGATIVE_INFINITY);
        chipPlaceholder.setMinWidth(Double.NEGATIVE_INFINITY);
        chipPlaceholder.setPrefHeight(100.0);
        chipPlaceholder.setPrefWidth(90.0);
        HBox.setHgrow(chipPlaceholder, Priority.NEVER);

        HBox chip = null;
        if (user.getRole() == Role.ADMIN) {
            chip = ChipMaker.createChip(Role.ADMIN.toString(), "chip-color");
        }
        else {
            chip = ChipMaker.createChip(Role.USER.toString(), "chip-color");
        }

        chipPlaceholder.getChildren().add(chip);

        // Right spacer
        Region rightSpacer = new Region();
        rightSpacer.setLayoutX(77.0);
        rightSpacer.setLayoutY(10.0);
        rightSpacer.setMaxWidth(Double.NEGATIVE_INFINITY);
        rightSpacer.setMinWidth(Double.NEGATIVE_INFINITY);
        rightSpacer.setPrefHeight(200.0);
        rightSpacer.setPrefWidth(9.0);

        outerHBox.getChildren().addAll(
                leftSpacer,
                checkBox,
                checkAvatarSpacer,
                avatar,
                avatarLabelSpacer,
                nameLabel,
                growingSpacer,
                chipPlaceholder,
                rightSpacer
        );

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {

            onCheckChangedUser.accept(user, newValue);

        });

        if (profile != null) {
            checkBox.setSelected(profile.getUsers().contains(user));
            System.out.println(profile.getUsers());
        }

        return outerHBox;
    }
}
