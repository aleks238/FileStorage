package com.fileStorage.backend.serverApp;

import com.fileStorage.backend.constants.Constants;
import com.fileStorage.backend.authService.AuthUsers;
import com.fileStorage.common.ByteObject;
import com.fileStorage.common.StringCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<ByteObject> {

    private final String space = " ";
    private final AuthUsers authUsers;
    private final ServerFunctionality serverFunctionality;
    private String userName;

    ClientHandler(AuthUsers authUsers) {
        this.authUsers = authUsers;
        serverFunctionality = new ServerFunctionality();
        log.info("Клиент подключился к серверу");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Канал открыт");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Канал закрыт");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channel, ByteObject object) {
        /*У каждого клиента свой clientHandler со своим каналом, поэтому каждый раз в метод сервера нужно передавать канал клиента */
        String className = object.getClass().getSimpleName();
        try {
            switch (className) {
                case "StringCommand":
                    processStringCommand(channel, object);
                    break;
                case "Directory":
                    serverFunctionality.storeDirectory(channel,userName, object);
                    break;
                case "CreateEmptyFile":
                    serverFunctionality.createEmptyFile(channel,userName, object);
                    break;
                case "FileSegment":
                    serverFunctionality.storeFileSegment(channel,userName, object);
                    break;
            }
        } catch (NoSuchFieldException
                 | IllegalAccessException
                 | SQLException
                 | IOException e) {
            e.printStackTrace();
        }
    }

    private void processStringCommand(ChannelHandlerContext channel, ByteObject object) throws NoSuchFieldException, IllegalAccessException, SQLException, IOException {
        StringCommand stringCommandObject = (StringCommand) object;
        String message = stringCommandObject.getMessage();
        String[] tokens = message.split(space);
        String command = tokens[0];

        switch (command) {
            case Constants.AUTH_COMMAND:
                performAuth(channel, message);
                break;
            case Constants.REGISTRATION_COMMAND:
                performRegistration(channel, message);
                break;
            case Constants.DOWNLOAD_COMMAND:
                serverFunctionality.performDownload(channel,message, userName);
                break;
            case Constants.DELETE_COMMAND:
                serverFunctionality.performDelete(channel,userName, message);
                break;
            case Constants.LIST_ONLY_FILES_COMMAND:
                serverFunctionality.performListFiles(channel,userName);
                break;
            case Constants.DISPLAY_STORAGE_COMMAND:
                serverFunctionality.sendStorageContent(channel,userName);
                break;
            case Constants.DISPLAY_FOLDER_CONTENT:
                serverFunctionality.displayFolderContent(channel,message);
                break;
            case Constants.DOWNLOAD_FILE_FROM_FILES:
                serverFunctionality.downloadFileFromFiles(channel,userName, message);
                break;
        }
    }

    private void performRegistration(ChannelHandlerContext channel, String str) throws SQLException {
        String[] tokens = str.split(space);
        String response = authUsers.saveNewUser(tokens[1], tokens[2]);
        channel.writeAndFlush(new StringCommand(response));
    }

    private void performAuth(ChannelHandlerContext channel, String str) {
        String[] tokens = str.split(space);
        String response = authUsers.checkUsernameAndPassword(tokens[1], tokens[2]);
        channel.writeAndFlush(new StringCommand(response));
        if (response.equals(Constants.AUTH_SUCCESSFUL)) {
            userName = tokens[1];
            serverFunctionality.sendStorageContent(channel,userName);
            log.debug("Клиент " + userName + " вошел в аккаунт");
        }
    }
}

