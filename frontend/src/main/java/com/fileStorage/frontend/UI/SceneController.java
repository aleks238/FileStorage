package com.fileStorage.frontend.UI;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneController {
    private static RegistrationPageController registrationPageController;
    private static MainPageController mainPageController;
    private Stage stage;
    private Scene scene;

    public void switchToLoginPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
        Parent root = loader.load();
        LoginPageController newReference = loader.getController();
        ClientApp.setLoginPageController(newReference);
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToRegistrationPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegistrationPage.fxml"));
        Parent root = loader.load();
        registrationPageController = loader.getController();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToMainPage() throws IOException {
        Stage mainStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainPage.fxml"));
        Parent root = loader.load();
        mainPageController = loader.getController();

        Scene scene = new Scene(root);
        mainStage.setScene(scene);
        mainStage.show();
    }

    public static RegistrationPageController getRegistrationPageController() {
        return registrationPageController;
    }

    public static MainPageController getMainPageController() {
        return mainPageController;
    }
}
