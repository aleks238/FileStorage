package com.fileStorage.backend.serverApp;

import com.fileStorage.backend.constants.Constants;
import com.fileStorage.common.*;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
public class ServerFunctionality {
    private final String storagePath = "server/users/";
    private final Object[] locks;
    private final static int space = 1;

    public ServerFunctionality() {
        locks = new Object[12];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }
    }

    void displayFolderContent(ChannelHandlerContext channel, String str) throws IOException {
        synchronized (locks[0]) {
            String folderPath = storagePath + str.substring(Constants.DISPLAY_FOLDER_CONTENT.length() + space);
            try (Stream<Path> stream = Files.list(Paths.get(folderPath))) {
                String[] folderContent = stream.map(Path::getFileName)
                        .map(Path::toString)
                        .toArray(java.lang.String[]::new);
                channel.writeAndFlush(new StorageList(folderContent));
            }
        }
    }

    void performDownload(ChannelHandlerContext channel, String str, String username) throws IOException {
        synchronized (locks[1]) {
            String filePath = str.substring(Constants.DOWNLOAD_COMMAND.length() + space);
            Path filePathOnServer = Paths.get(storagePath + filePath);
            String fileName = filePathOnServer.getFileName().toString();
            if (Files.isDirectory(filePathOnServer)) {
                sendDirectory(channel, filePathOnServer, username);
            } else {
                sendFileSegments(channel, filePathOnServer, fileName);
            }
        }
    }

    void sendDirectory(ChannelHandlerContext channel, Path source, String username) throws IOException {
        synchronized (locks[2]) {
            Path serverDirectories = Paths.get(storagePath + username);
            if (Files.isDirectory(source)) {
                Path directoryRelativePath = serverDirectories.relativize(source);
                channel.writeAndFlush(new Directory(directoryRelativePath.toString()));
                try (Stream<Path> paths = Files.list(source)) {
                    paths.forEach(path -> recursiveSendDirectory(channel, path, username));
                }
            } else {
                Path fileRelativePath = serverDirectories.relativize(source);
                sendFileSegments(channel, source, fileRelativePath.toString());

            }
        }
    }

    void recursiveSendDirectory(ChannelHandlerContext channel, Path source, String username) {
        synchronized (locks[3]) {
            try {
                sendDirectory(channel, source, username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendFileSegments(ChannelHandlerContext channel, Path filePathOnServer, String fileName) throws IOException {
        synchronized (locks[4]) {
                byte[] buffer = new byte[1024];
                int read;
                long position = 0;
                channel.writeAndFlush(new CreateEmptyFile(fileName));
                try (FileInputStream fileInputStream = new FileInputStream(filePathOnServer.toString())) {
                    while ((read = fileInputStream.read(buffer)) != -1) {
                        position += read;
                        channel.writeAndFlush(new FileSegment(fileName, read, position, buffer));
                        buffer = new byte[1024];
                    }
                }
        }
    }

    void downloadFileFromFiles(ChannelHandlerContext channel, String userName, String str) throws IOException {
        synchronized (locks[5]) {
            String fileName = str.substring(Constants.DOWNLOAD_FILE_FROM_FILES.length() + space);
            try (Stream<Path> stream = Files.walk(Paths.get(storagePath + userName))) {
                Path path = stream.filter(file -> file.getFileName().toString().equals(fileName))
                        .findAny()
                        .orElse(null);
                if (path != null) {
                    sendFileSegments(channel, path, fileName);
                    log.debug("Клиент загрузил файл " + fileName);
                }
            }
        }
    }

    void performListFiles(ChannelHandlerContext channel, String userName) throws IOException {
        synchronized (locks[6]) {
            Path path = Paths.get(storagePath + userName);
            try (Stream<Path> stream = Files.walk(path)) {
                String[] filesArray = stream.filter(file -> !Files.isDirectory(file))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .toArray(java.lang.String[]::new);
                channel.writeAndFlush(new StorageList(filesArray));
            }
        }

    }

    void performDelete(ChannelHandlerContext channel, String userName, String str) throws IOException {
        synchronized (locks[7]) {
            String fileName = str.substring(Constants.DELETE_COMMAND.length() + space);
            Path filePath;
            try (Stream<Path> stream = Files.list(Paths.get(storagePath + userName))) {
                filePath = stream.filter(file -> file.getFileName().toString().equals(fileName))
                        .findAny()
                        .orElse(null);
            }
            if (Files.isDirectory(filePath)) {
                try (Stream<Path> stream = Files.walk(filePath)) {
                    stream.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    log.debug("клиент " + userName + " удалил директорию " + filePath);
                    channel.writeAndFlush(new StringCommand("#Файлы удалены#"));
                }
            } else {
                Files.deleteIfExists(filePath);
                log.debug("клиент " + userName + " удалил файл " + filePath);
                channel.writeAndFlush(new StringCommand("#Файлы удалены#"));
            }
        }
    }

    void sendStorageContent(ChannelHandlerContext channel, String userName) {
        synchronized (locks[8]) {
            File clientDirectory = new File(storagePath + userName);
            String[] clientContent = clientDirectory.list();
            channel.writeAndFlush(new StorageList(clientContent));
        }
    }

    void storeFileSegment(ChannelHandlerContext channel, String userName, ByteObject object) throws
            IOException {
        synchronized (locks[9]) {
            FileSegment file = (FileSegment) object;
            int read = file.getRead();
            long position = file.getPosition();
            byte[] buffer = file.getBuffer();
            String filePathInFolder = file.getFilePath();
            Path filePathOnServer = Paths.get(storagePath + userName + "/" + filePathInFolder);

            try (RandomAccessFile writeFile = new RandomAccessFile(filePathOnServer.toString(), "rw")) {
                writeFile.seek(position - read);
                writeFile.write(buffer, 0, read);
            }
            channel.writeAndFlush(new StringCommand("#Загрузка файлов завершена#"));
            log.debug("Клиент " + userName + " загрузил файл " + filePathOnServer);
        }
    }

    void createEmptyFile(ChannelHandlerContext channel, String userName, ByteObject object) throws IOException {
        synchronized (locks[10]) {
            CreateEmptyFile emptyFile = (CreateEmptyFile) object;
            String filePath = emptyFile.getFilePath();
            Path filePathOnServer = Paths.get(storagePath + userName + "/" + filePath);
            if (Files.notExists(filePathOnServer)) {
                Files.createFile(filePathOnServer);
            } else {
                channel.writeAndFlush(new StringCommand("#Файлы уже существуют#"));
            }
        }
    }

    void storeDirectory(ChannelHandlerContext channel, String userName, ByteObject object) throws NoSuchFieldException, IllegalAccessException, IOException {
        synchronized (locks[11]) {
            Directory directoryObject = (Directory) object;
            String relativePath = directoryObject.getRelativePath();
            Path foldersPath = Paths.get(storagePath + userName + "/" + relativePath);
            if (Files.notExists(foldersPath)) {
                Files.createDirectories(foldersPath);
            } else {
                channel.writeAndFlush(new StringCommand("#Папка уже существует#"));
            }
            log.debug("Клиент " + userName + " загрузил папку " + foldersPath);
        }
    }
}