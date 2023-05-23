package com.fileStorage.backend.databaseService;
import java.sql.SQLException;

public interface AuthService {
    String checkUsernameAndPassword(String login, String password);
    String saveNewUser(String userName, String password) throws SQLException;
    boolean isPresent(String userName);
}
