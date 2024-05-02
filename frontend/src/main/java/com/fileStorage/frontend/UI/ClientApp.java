package com.fileStorage.frontend.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private static LoginPageController loginPageController;
    @Override
    public void start(Stage authStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
        Parent root = loader.load();
        loginPageController = loader.getController();
        Scene scene = new Scene(root);
        authStage.setScene(scene);
        authStage.show();
    }
    public static LoginPageController getLoginPageController() {
        return loginPageController;
    }
    public static void setLoginPageController(LoginPageController loginPageController) {
        ClientApp.loginPageController = loginPageController;
    }
}
