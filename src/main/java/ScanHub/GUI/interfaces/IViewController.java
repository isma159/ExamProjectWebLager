package ScanHub.GUI.interfaces;

import ScanHub.GUI.facade.ModelFacade;
import javafx.stage.Stage;

public interface IViewController {
    void setModel(ModelFacade model, Stage stage);
}
