package se.fulkopinglibrary.fulkopinglibrary.utils;

import se.fulkopinglibrary.fulkopinglibrary.models.User;
import se.fulkopinglibrary.fulkopinglibrary.services.UserService;

import java.sql.Connection;
import java.util.Scanner;

public class Login {
    // Delegates directly to UserService's login implementation
    public static User login(Connection connection, Scanner scanner) {
        return UserService.login(connection, scanner);
    }
}
