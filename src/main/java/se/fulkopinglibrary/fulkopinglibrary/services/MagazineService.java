package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import se.fulkopinglibrary.fulkopinglibrary.utils.LoggerUtil;

public class MagazineService {
    public static boolean borrowMagazine(Connection connection, int userId, int magazineId) {
        try {
            String sql = "UPDATE library_items SET is_available = false WHERE item_id = ? AND type = 'MAGAZINE'";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, magazineId);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    sql = "INSERT INTO loans (user_id, item_id, loan_date) VALUES (?, ?, CURRENT_DATE)";
                    try (PreparedStatement loanStmt = connection.prepareStatement(sql)) {
                        loanStmt.setInt(1, userId);
                        loanStmt.setInt(2, magazineId);
                        loanStmt.executeUpdate();
                    }
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            LoggerUtil.getLogger(MagazineService.class).severe("Error borrowing magazine: " + e.getMessage());
            return false;
        }
    }
    private static final Logger logger = LoggerUtil.getLogger(MagazineService.class);

    public static List<Magazine> getAllItems(Connection connection) throws SQLException {
        return getAllItems(connection, 0, 1, 20);
    }

    public static List<Magazine> getAllItems(Connection connection, int sortOption, int page, int pageSize) throws SQLException {
        List<Magazine> items = new ArrayList<>();
        
        String orderBy = switch (sortOption) {
            case 1 -> "title ASC";
            case 2 -> "title DESC";
            case 3 -> "is_available DESC";
            default -> "item_id ASC";
        };
        
        String query = """
            SELECT item_id, title, publisher, issn, is_available 
            FROM library_items 
            WHERE type = 'MAGAZINE'
            ORDER BY %s
            LIMIT ? OFFSET ?""".formatted(orderBy);
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int offset = (page - 1) * pageSize;
            statement.setInt(1, pageSize);
            statement.setInt(2, offset);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Magazine magazine = new Magazine(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getString("publisher"),
                        resultSet.getString("issn"),
                        resultSet.getBoolean("is_available")
                    );
                    items.add(magazine);
                }
            }
        }
        return items;
    }

    public static boolean isItemAvailable(Connection connection, int magazineId) {
        String query = "SELECT is_available FROM library_items WHERE item_id = ? AND type = 'MAGAZINE'";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, magazineId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("is_available");
                }
            }
        } catch (SQLException e) {
            logger.severe("Error checking magazine availability: " + e.getMessage());
        }
        return false;
    }

    public static boolean reserveMagazine(Connection connection, int userId, int magazineId) {
        String checkReservationSql = """
            SELECT reservation_id FROM reservations 
            WHERE user_id = ? AND item_id = ? AND expiry_date > CURRENT_DATE
            """;
            
        String insertReservationSql = """
            INSERT INTO reservations 
            (user_id, item_id, reservation_date, expiry_date) 
            VALUES (?, ?, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY))
            """;
            
        try {
            // Check if user already has an active reservation
            try (PreparedStatement checkStmt = connection.prepareStatement(checkReservationSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, magazineId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return false; // Reservation already exists
                    }
                }
            }
            
            // Create new reservation
            try (PreparedStatement insertStmt = connection.prepareStatement(insertReservationSql)) {
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, magazineId);
                int rowsInserted = insertStmt.executeUpdate();
                return rowsInserted > 0;
            }
        } catch (SQLException e) {
            logger.severe("Error reserving magazine: " + e.getMessage());
            return false;
        }
    }

    public static List<Magazine> searchMagazines(Connection connection, String searchType, String searchTerm) throws SQLException {
        List<Magazine> magazines = new ArrayList<>();
        String query = "SELECT item_id, title, publisher, issn, is_available FROM library_items WHERE type = 'MAGAZINE'";
        
        // Add search conditions based on searchType
        switch (searchType.toLowerCase()) {
            case "title":
                query += " AND title LIKE ?";
                break;
            case "publisher":
                query += " AND publisher LIKE ?";
                break;
            case "issn":
                query += " AND issn LIKE ?";
                break;
            default:
                query += " AND (title LIKE ? OR publisher LIKE ? OR issn LIKE ?)";
                break;
        }

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            if (searchType.equalsIgnoreCase("title") || searchType.equalsIgnoreCase("publisher") || searchType.equalsIgnoreCase("issn")) {
                statement.setString(1, "%" + searchTerm + "%");
            } else {
                statement.setString(1, "%" + searchTerm + "%");
                statement.setString(2, "%" + searchTerm + "%");
                statement.setString(3, "%" + searchTerm + "%");
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Magazine magazine = new Magazine(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getString("publisher"),
                        resultSet.getString("issn"),
                        resultSet.getBoolean("is_available")
                    );
                    magazines.add(magazine);
                }
            }
        }
        return magazines;
    }

}
