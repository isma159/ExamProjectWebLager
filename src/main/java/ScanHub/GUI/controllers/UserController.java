package ScanHub.GUI.controllers;

// project imports
import ScanHub.GUI.facade.ModelFacade;

// java imports
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    private Stage currentStage;
    private ModelFacade modelFacade;

    public UserController() {}

    public void setModel(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
