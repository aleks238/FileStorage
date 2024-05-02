package com.fileStorage.frontend.network;

import com.fileStorage.common.*;
import com.fileStorage.frontend.UI.ClientApp;
import com.fileStorage.frontend.UI.SceneController;

import com.fileStorage.frontend.constants.Constants;
import javafx.application.Platform;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CallbackImpl implements Callback {
    @Override
    public void onReceived(ByteObject object) {
        String className = object.getClass().getSimpleName();
        try {
            switch (className) {
                case "StringCommand":
                    processStringCommand(object);
                    break;
                case "StorageList":
                    processStorageList(object);
                    break;
                case "CreateEmptyFile":
                    createEmptyFile(object);
                    break;
                case "FileSegment":
                    storeFileSegment(object);
                    break;
                case "Directory":
                    downloadDirectory(object);
                    break;

            }
        } catch (IOException
                 | NoSuchFieldException
                 | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void processStringCommand(ByteObject object) {
        StringCommand stringCommand = (StringCommand) object;
        String command = stringCommand.getMessage();

        switch (command) {
            case "#Пользователь не существует#":
                Platform.runLater(() -> ClientApp.getLoginPageController().userNotExist());
                break;
            case "#Неверный пароль#":
                Platform.runLater(() -> ClientApp.getLoginPageController().wrongPassword());
                break;
            case Constants.AUTH_SUCCESSFUL:
                Platform.runLater(() -> ClientApp.getLoginPageController().authCorrect());
                break;
            case "#Такой пользователь уже зарегистрирован#":
                Platform.runLater(() -> SceneController.getRegistrationPageController().userPresentInDb());
                break;
            case "#Регистрация успешна#":
                Platform.runLater(() -> SceneController.getRegistrationPageController().registrationSuccessful());
                break;
            case "#Регистрация временно не работает#":
                Platform.runLater(() -> SceneController.getRegistrationPageController().registrationProblem());
                break;
            case "#Файлы уже существуют#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Файлы уже существует в хранилище"));
                break;
            case "#Папка уже существует#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Папка уже существует в хранилище"));
                break;
            case "#Загрузка файлов завершена#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Загрузка файлов завершена"));
                break;
            case "#Файлы удалены#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Файлы удалены"));
                break;
        }
    }

    private void downloadDirectory(ByteObject object) throws NoSuchFieldException, IllegalAccessException, IOException {
        Directory directoryObject = (Directory) object;
        String directoryRelativePath = directoryObject.getRelativePath();
        Path downloadPath = Paths.get(System.getProperty("user.home") + "/" + "Downloads" + "/" + directoryRelativePath);
        if (Files.notExists(downloadPath)) {
            Files.createDirectories(downloadPath);
        } else {
            Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Такая папка уже существует"));
        }
    }

    private void createEmptyFile(ByteObject object) throws IOException {
        CreateEmptyFile emptyFile = (CreateEmptyFile) object;
        String filePath = emptyFile.getFilePath();
        Path downloadFilePath = Paths.get(System.getProperty("user.home") + "/" + "Downloads" + "/" + filePath);
        if (Files.notExists(downloadFilePath)) {
            Files.createFile(downloadFilePath);
        } else {
            Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Такой файл уже существует"));
        }
    }

    private void storeFileSegment(ByteObject object) throws IOException {
        FileSegment file = (FileSegment) object;
        int read = file.getRead();
        long position = file.getPosition();
        byte[] buffer = file.getBuffer();
        String filePath = file.getFilePath();
        Path downloadFilePath = Paths.get(System.getProperty("user.home") + "/" + "Downloads" + "/" + filePath);
        try (RandomAccessFile writeFile = new RandomAccessFile(downloadFilePath.toString(), "rw")) {
            writeFile.seek(position - read);
            writeFile.write(buffer, 0, read);
        }
    }

    private void processStorageList(ByteObject object) {
        StorageList storageList = (StorageList) object;
        String[] clientContent = storageList.getClientContent();
        Platform.runLater(() -> SceneController.getMainPageController().displayContent(clientContent));
    }
}
