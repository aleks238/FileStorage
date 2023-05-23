package com.fileStorage.frontend.UI;

import com.fileStorage.common.CreateEmptyFile;
import com.fileStorage.common.Directory;
import com.fileStorage.common.FileSegment;
import com.fileStorage.common.StringCommand;
import com.fileStorage.frontend.constants.Constants;
import com.fileStorage.frontend.network.NettyNet;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class MainPageController implements Initializable {
    @FXML
    private Label resultLabel;
    @FXML
    private MenuButton menuButton;
    @FXML
    private Label pathLabel;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ListView<String> listView;
    private NettyNet net;
    private File selectedDirectory;
    private Image folder;
    private Image file;
    private String userName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = LoginPageController.getNet();
        userName = LoginPageController.getTrueUserName();
        pathLabel.setText(userName);

        folder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/folder.png")));
        file = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/file.png")));
        Image plusImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/plus.png")));

        ImageView view = new ImageView(plusImage);
        view.setFitHeight(30);
        view.setPreserveRatio(true);
        menuButton.setGraphic(view);

        setDoubleClick();
        setIconsAndContextMenu();
    }

    private void setDoubleClick() {
        listView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                String folderName = listView.getSelectionModel().getSelectedItem();
                if (!folderName.contains(".")) {
                    pathLabel.setText(pathLabel.getText() + "/" + folderName);
                    String folderPath = pathLabel.getText();
                    net.sendObject(new StringCommand(Constants.DISPLAY_FOLDER_CONTENT + " " + folderPath));
                }
            }
        });
    }

    public void displayContent(java.lang.String[] files) {
        listView.getItems().clear();
        for (String file : files) {
            listView.getItems().add(file);
        }
    }

    public void displayAllFiles() {
        hideResultLabel();
        pathLabel.setText("Все файлы:");
        listView.getItems().clear();
        net.sendObject(new StringCommand(Constants.LIST_ONLY_FILES_COMMAND));
    }

    public void displayStorageCommand() {
        hideResultLabel();
        pathLabel.setText(userName);
        listView.getItems().clear();
        net.sendObject(new StringCommand(Constants.DISPLAY_STORAGE_COMMAND));
    }

    public void displayResultLabel(String string) {
        resultLabel.setStyle("-fx-border-color: green;");
        resultLabel.setWrapText(true);
        resultLabel.setTextAlignment(TextAlignment.JUSTIFY);
        resultLabel.setText(string);
    }

    private void hideResultLabel() {
        resultLabel.setStyle("-fx-border-color: none;");
        resultLabel.setText("");
    }

    private void download(String item) {
        String filePath = pathLabel.getText() + "/" + item;
        net.sendObject(new StringCommand(Constants.DOWNLOAD_COMMAND + " " + filePath));
    }

    private void downloadFileFromFiles(String item) {
        net.sendObject(new StringCommand(Constants.DOWNLOAD_FILE_FROM_FILES + " " + item));
    }


    public void chooseFiles() {
        hideResultLabel();
        FileChooser fileChooser = new FileChooser();
        final List<File> files;
        files = fileChooser.showOpenMultipleDialog(null);
        try {
            if (files != null) {
                for (File file : files) {
                        String fileName = file.getName();
                        sendFileSegments(file, fileName);
                        addToListView(fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFileSegments(File file, String filePath) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        long position = 0;
        net.sendObject(new CreateEmptyFile(filePath));
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            while ((read = fileInputStream.read(buffer)) != -1) {
                position += read;
                net.sendObject(new FileSegment(filePath, read, position, buffer));
                buffer = new byte[1024];
            }
        }
    }

    public void chooseDirectory() throws IOException {
        hideResultLabel();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) borderPane.getScene().getWindow();
        selectedDirectory = directoryChooser.showDialog(stage);
        sendDirectory(selectedDirectory.toPath());
        addToListView(selectedDirectory.getName());
    }

    void sendDirectory(Path source) throws IOException {
        if (Files.isDirectory(source)) {
            Path directoryRelativePath = Paths.get(selectedDirectory.getName() + "/" + selectedDirectory.toPath().relativize(source));
            net.sendObject(new Directory(directoryRelativePath.toString()));
            try (Stream<Path> paths = Files.list(source)) {
                paths.forEach(this::recursiveSendDirectory);
            }
        } else {
            String fileRelativePath = selectedDirectory.getName() + "/" + selectedDirectory.toPath().relativize(source);
            sendFileSegments(source.toFile(), fileRelativePath);
        }
    }

    void recursiveSendDirectory(Path source) {
        try {
            sendDirectory(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToListView(String fileName) {
        ObservableList<String> listviewItems = listView.getItems();
        if (!listviewItems.contains(fileName)) {
            listviewItems.add(fileName);
        }
    }

    private void delete(String item) {
        net.sendObject(new StringCommand(Constants.DELETE_COMMAND + " " + item));
    }

    private void setIconsAndContextMenu() {
        listView.setCellFactory(listView -> {
            ListCell<String> cell = new ListCell<String>() {
                private final ImageView imageView = new ImageView();

                @Override
                public void updateItem(String name, boolean empty) {

                    imageView.setFitHeight(16);
                    imageView.setFitWidth(16);
                    super.updateItem(name, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (name.contains(".")) {
                            imageView.setImage(file);
                        } else {
                            imageView.setImage(folder);
                        }
                        setText(name);
                        setGraphic(imageView);
                    }
                }
            };
            ContextMenu contextMenu = new ContextMenu();
            MenuItem downloadItem = new MenuItem();
            downloadItem.textProperty().bind(Bindings.format("Download        ", cell.itemProperty()));
            downloadItem.setOnAction(event -> {
                String item = cell.getItem();
                if (pathLabel.getText().equals("Все файлы:")) {
                    downloadFileFromFiles(item);
                } else {
                    download(item);
                }
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete        ", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                String item = cell.getItem();
                delete(item);
                listView.getItems().remove(cell.getItem());
            });
            contextMenu.getItems().addAll(downloadItem, deleteItem);
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }
}


