package ScanHub.GUI.controllers;

import ScanHub.GUI.facade.ModelFacade;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    private ModelFacade modelFacade;

    public UserController() throws Exception {
    }

    public void setModel(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
