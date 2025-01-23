package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.User;
import java.util.Set;
import java.util.HashSet;
import se.fulkopinglibrary.fulkopinglibrary.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private static final int MIN_PASSWORD_LENGTH = 8;

    public static boolean signup(Connection connection, Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        
        if (!User.isValidUsername(username)) {
            logger.log(Level.WARNING, "Invalid username format");
            return false;
        }

        // Password entry with validation loop
        String password;
        while (true) {
            System.out.println("\nPassword Requirements:");
            System.out.println(PasswordUtils.PASSWORD_REQUIREMENTS);
            System.out.println("Example of strong password: SecurePass123!");
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            
            String validationResult = PasswordUtils.validatePasswordStrength(password);
            if (validationResult == null) {
                break;
            }
            System.out.println("\nPassword is not strong enough:");
            System.out.println(validationResult);
            System.out.println("Please try again with a stronger password.\n");
        }

        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        if (!User.isValidEmail(email)) {
            logger.log(Level.WARNING, "Invalid email format");
            return false;
        }

        // Check if username already exists
        if (usernameExists(connection, username)) {
            logger.log(Level.WARNING, "Username already exists");
            return false;
        }

        // Generate salt and hash the password
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);

        String userQuery = """
            INSERT INTO users (
                username, password_hash, salt, name, email,
                failed_attempts, lockout_until, created_at, updated_at, is_deleted
            ) VALUES (?, ?, ?, ?, ?, 0, NULL, NOW(), NOW(), false)
            """;
            
        String roleQuery = """
            INSERT INTO user_roles (user_id, role_id)
            SELECT ?, role_id FROM roles WHERE role_name = 'USER'
            """;
        
        try {
            handleTransaction(connection, () -> {
                try (PreparedStatement userStatement = connection.prepareStatement(userQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                     PreparedStatement roleStatement = connection.prepareStatement(roleQuery)) {
                    
                    // Insert user
                    userStatement.setString(1, username);
                    userStatement.setString(2, passwordHash);
                    userStatement.setString(3, salt);
                    userStatement.setString(4, name);
                    userStatement.setString(5, email);
                    
                    int rowsInserted = userStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        // Get generated user ID
                        try (ResultSet generatedKeys = userStatement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int userId = generatedKeys.getInt(1);
                                
                                // Insert default USER role
                                roleStatement.setInt(1, userId);
                                roleStatement.executeUpdate();
                                
                                logger.log(Level.INFO, "User successfully registered: " + username);
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Database error during signup", e);
                }
            });
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Transaction failed during signup", e);
        }
        return false;
    }

    public static boolean updateProfile(Connection connection, int userId, Scanner scanner) {
        System.out.print("Enter new name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter new email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter new password (leave blank to keep current): ");
        String newPassword = scanner.nextLine().trim();

        // Validate inputs
        if (name.isBlank()) {
            logger.log(Level.WARNING, "Name cannot be blank");
            return false;
        }
        if (!User.isValidEmail(email)) {
            logger.log(Level.WARNING, "Invalid email format");
            return false;
        }
        if (!newPassword.isEmpty() && !isPasswordStrong(newPassword)) {
            logger.log(Level.WARNING, "New password does not meet strength requirements");
            return false;
        }

        String query = newPassword.isEmpty() 
            ? "UPDATE users SET name = ?, email = ?, updated_at = NOW() WHERE user_id = ?"
            : "UPDATE users SET name = ?, email = ?, password_hash = ?, salt = ?, updated_at = NOW() WHERE user_id = ?";

        try {
            handleTransaction(connection, () -> {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, name);
                    statement.setString(2, email);
                    
                    if (newPassword.isEmpty()) {
                        statement.setInt(3, userId);
                    } else {
                        String salt = PasswordUtils.generateSalt();
                        String passwordHash = PasswordUtils.hashPassword(newPassword, salt);
                        statement.setString(3, passwordHash);
                        statement.setString(4, salt);
                        statement.setInt(5, userId);
                    }
                    
                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated > 0) {
                        logger.log(Level.INFO, "Profile updated for user ID: " + userId);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Database error during profile update", e);
                }
            });
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Transaction failed during profile update", e);
        }
        return false;
    }

    public static boolean updatePassword(Connection connection, int userId, String newPasswordHash, String newSalt) {
        String query = "UPDATE users SET password_hash = ?, salt = ?, updated_at = NOW() WHERE user_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newPasswordHash);
            statement.setString(2, newSalt);
            statement.setInt(3, userId);
            
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating password", e);
            return false;
        }
    }

    private static boolean usernameExists(Connection connection, String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking username existence", e);
            return false;
        }
    }

    private static boolean isPasswordStrong(String password) {
        String validationResult = PasswordUtils.validatePasswordStrength(password);
        if (validationResult != null) {
            logger.log(Level.WARNING, validationResult);
            return false;
        }
        return true;
    }

    private static void handleTransaction(Connection connection, Runnable operation) throws SQLException {
        try {
            connection.setAutoCommit(false);
            operation.run();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    public static User login(Connection connection, Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (username.isBlank() || password.isBlank()) {
            logger.log(Level.WARNING, "Username and password cannot be blank");
            return null;
        }

        String query = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            logger.log(Level.WARNING, "User not found: " + username);
                            return null;
                        }

                        // Check if account is locked
                        Timestamp lockoutUntil = rs.getTimestamp("lockout_until");
                        if (lockoutUntil != null && lockoutUntil.after(new Timestamp(System.currentTimeMillis()))) {
                            logger.log(Level.WARNING, "Account locked for user: " + username);
                            return null;
                        }

                        String storedHash = rs.getString("password_hash");
                        String salt = rs.getString("salt");
                        
                        // Verify password using current hashing method
                        boolean passwordValid = PasswordUtils.verifyPassword(password, storedHash, salt);
                        
                        if (!passwordValid) {
                            // Increment failed attempts only if user exists but password is wrong
                            int attempts = incrementFailedAttempts(connection, rs.getInt("user_id"));
                            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                                lockAccount(connection, rs.getInt("user_id"));
                                logger.log(Level.WARNING, "Account locked due to too many failed attempts: " + username);
                            }
                            return null;
                        }

                        // Reset failed attempts on successful login
                        resetFailedAttempts(connection, rs.getInt("user_id"));
                        
                        // Get roles from join table
                        Set<String> roles = new HashSet<>();
                        String rolesQuery = """
                            SELECT r.role_name 
                            FROM user_roles ur
                            JOIN roles r ON ur.role_id = r.role_id
                            WHERE ur.user_id = ?
                            """;
                        try (PreparedStatement rolesStatement = connection.prepareStatement(rolesQuery)) {
                            rolesStatement.setInt(1, rs.getInt("user_id"));
                            try (ResultSet rolesRs = rolesStatement.executeQuery()) {
                                while (rolesRs.next()) {
                                    roles.add(rolesRs.getString("role_name"));
                                }
                            }
                        }
                    
                        return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("salt"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("failed_attempts"),
                            rs.getTimestamp("lockout_until") != null ? 
                                rs.getTimestamp("lockout_until").toLocalDateTime() : null,
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime(),
                            rs.getBoolean("is_deleted"),
                            roles
                        );
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during login", e);
        }
        return null;
    }

    private static void resetFailedAttempts(Connection connection, int userId) throws SQLException {
        String query = "UPDATE users SET failed_attempts = 0, lockout_until = NULL WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    private static int incrementFailedAttempts(Connection connection, int userId) throws SQLException {
        String updateQuery = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
        String selectQuery = "SELECT failed_attempts FROM users WHERE user_id = ?";
        
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            
            updateStatement.setInt(1, userId);
            updateStatement.executeUpdate();
            
            selectStatement.setInt(1, userId);
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("failed_attempts");
                }
            }
        }
        return 0;
    }

    private static void lockAccount(Connection connection, int userId) throws SQLException {
        try {
            connection.setAutoCommit(false);
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, LOCKOUT_MINUTES);
            Timestamp lockoutUntil = new Timestamp(cal.getTimeInMillis());
            
            String query = "UPDATE users SET lockout_until = ? WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setTimestamp(1, lockoutUntil);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
