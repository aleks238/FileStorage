package com.fileStorage.backend.authService;

import com.fileStorage.backend.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
@Slf4j
public class AuthUsers implements AuthService {
    private static final String URL = "jdbc:mysql://localhost:3306/file_storage_database";
    private static final String userNameDB = "root";
    private static final String passwordDB = "root";
    private final Object addNewUserLock = new Object();
    private final Object checkUsernameAndPasswordLock = new Object();
    private Connection connection;

    public AuthUsers() {
        try {
            connection = DriverManager.getConnection(URL, userNameDB, passwordDB);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String checkUsernameAndPassword(String username, String password) {
        synchronized (checkUsernameAndPasswordLock) {
            String login = null;
            String userPassword = null;
            String query = "SELECT login, password FROM users WHERE login = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        login = rs.getString("login");
                        userPassword = rs.getString("password");
                    }
                }
                if (username.equals(login) && password.equals(userPassword)) {
                    return Constants.AUTH_SUCCESSFUL;
                }
                if (login == null) {
                    return "#Пользователь не существует#";
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "#Неверный пароль#";
        }
    }

    public String saveNewUser(String userName, String password) throws SQLException {
        synchronized (addNewUserLock) {
            if (isPresent(userName)) {
                return "#Такой пользователь уже зарегистрирован#";
            } else {
                String query = "INSERT INTO users (login, password) VALUES (?,?)";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, userName);
                    ps.setString(2, password);
                    int counter = ps.executeUpdate();
                    if (counter != 0) {
                        Files.createDirectory(Paths.get("server/users/" + userName));
                        return "#Регистрация успешна#";
                    }
                } catch (IOException e) {
                    log.error("exception=", e);
                }
            }
            return "#Регистрация временно не работает#";
        }
    }

    public boolean isPresent(String userName) {
        String query = "SELECT * FROM users WHERE login = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
