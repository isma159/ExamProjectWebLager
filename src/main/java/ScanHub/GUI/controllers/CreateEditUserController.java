package ScanHub.GUI.controllers;

import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;

public class CreateEditUserController {

    private ModelFacade modelFacade;
    private User editingUser = null;
    private boolean editMode = false;

    public CreateEditUserController() {
        try {
            modelFacade = new ModelFacade();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
