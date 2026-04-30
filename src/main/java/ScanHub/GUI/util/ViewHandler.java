package ScanHub.GUI.util;

//java imports
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public enum ViewHandler {
    LOGIN("/views/LoginView.fxml", "Login", false, Modality.APPLICATION_MODAL),
    ADMIN("/views/AdminView.fxml", "Admin Panel", true, Modality.WINDOW_MODAL),
    USER("/views/UserView.fxml", "User Panel", true, Modality.WINDOW_MODAL),
    SCAN_VIEW("/views/ScanView.fxml", "Scanning Workspace", true, Modality.WINDOW_MODAL),
    CREATE_USER("/views/UserFormView.fxml", "Create User", false, Modality.APPLICATION_MODAL),
    EDIT_USER("/views/UserFormView.fxml", "Edit User", false, Modality.APPLICATION_MODAL),
    CREATE_PROFILE("/views/ProfileFormView.fxml", "Create Profile", false, Modality.APPLICATION_MODAL),
    EDIT_PROFILE("/views/ProfileFormView.fxml", "Edit Profile", false, Modality.APPLICATION_MODAL);

    private final String path;
    private final String title;
    private FXMLLoader loader;
    private Scene scene;
    private Stage stage;

    private final boolean resizable;
    private final Modality modality;

    ViewHandler(String path, String title, boolean resizable, Modality modality) {
        this.path = path;
        this.title = title;
        this.resizable = resizable;
        this.modality = modality;
    }

    private Scene loadScene() throws IOException {
        if(scene == null) {
            loader = new FXMLLoader(getClass().getResource(path));
            scene = new Scene(loader.load());
        }
        return scene;
    }

    public void reset() {
        scene = null;
        stage = null;
        loader = null;
    }

    public void preLoad() {
        try {
            loadScene();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to pre-load: " + title);
        }
    }

    public Stage show() {
        try {
            Stage stage = getOrCreateStage();
            stage.show();
            return stage;
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to open: " + title);
            e.printStackTrace();
            return null;
        }
    }

    public void showAndWait() {
        try {
            getOrCreateStage().showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Error", "Could not open " + title);
            e.printStackTrace();
        }
    }

    public Parent getRoot() throws IOException {
        if (scene == null) loadScene();
        return (Parent) scene.getRoot();
    }

    private Stage getOrCreateStage() throws IOException {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle(title);
            stage.setScene(loadScene());
            stage.initModality(modality);
            stage.setResizable(resizable);
        }
        return stage;
    }

    /**
     * The view must be loaded before calling this method (via show() or preLoad())
     * @return the controller associated with this view.
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T> T getController() {
        if (loader == null) {
            throw new IllegalStateException(title + " has not been loaded yet. Call show() or preLoad() first.");
        }
        return (T) loader.getController();
    }
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}