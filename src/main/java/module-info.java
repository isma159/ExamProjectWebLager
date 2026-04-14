module dk.easv.examprojectweblager {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.examprojectweblager to javafx.fxml;
    exports dk.easv.examprojectweblager;
}