package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.MediaItem;
import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaTypeImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import se.fulkopinglibrary.fulkopinglibrary.utils.LoggerUtil;

public class MediaService {
    private static final Logger logger = LoggerUtil.getLogger(MediaService.class);

    public static List<LibraryItem> getAllItems(Connection connection) throws SQLException {
        return getAllItems(connection, 0, 1, 20);
    }

    public static List<LibraryItem> getAllItems(Connection connection, int sortChoice, int currentPage, int pageSize) throws SQLException {
        List<LibraryItem> items = new ArrayList<>();
        
        String orderBy = switch (sortChoice) {
            case 1 -> "title ASC";
            case 2 -> "title DESC";
            case 3 -> "is_available DESC";
            default -> "item_id ASC";
        };
        
        int offset = (currentPage - 1) * pageSize;
        
        String query = """
            SELECT item_id, title, director, catalog_number, type, is_available 
            FROM library_items 
            WHERE type = 'MEDIA'
            ORDER BY %s
            LIMIT %d OFFSET %d""".formatted(orderBy, pageSize, offset);
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MediaTypeImpl mediaType = MediaTypeImpl.fromString(resultSet.getString("type"));
                    MediaItem media = new MediaItem(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getBoolean("is_available"),
                        resultSet.getString("director"),
                        resultSet.getString("catalog_number"),
                        mediaType
                    );
                    items.add(media);
                }
            }
        }
        return items;
    }

    public static List<LibraryItem> searchByTitle(Connection connection, String title) throws SQLException {
        List<LibraryItem> items = new ArrayList<>();
        
        String query = """
            SELECT item_id, title, director, catalog_number, type, is_available 
            FROM library_items 
            WHERE type = 'MEDIA' AND title LIKE ?""";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + title + "%");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MediaTypeImpl mediaType = MediaTypeImpl.fromString(resultSet.getString("type"));
                    MediaItem media = new MediaItem(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getBoolean("is_available"),
                        resultSet.getString("director"),
                        resultSet.getString("catalog_number"),
                        mediaType
                    );
                    items.add(media);
                }
            }
        }
        return items;
    }

    public static List<LibraryItem> searchByDirector(Connection connection, String director) throws SQLException {
        List<LibraryItem> items = new ArrayList<>();
        
        String query = """
            SELECT item_id, title, director, catalog_number, type, is_available 
            FROM library_items 
            WHERE type = 'MEDIA' AND director LIKE ?""";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + director + "%");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MediaTypeImpl mediaType = MediaTypeImpl.fromString(resultSet.getString("type"));
                    MediaItem media = new MediaItem(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getBoolean("is_available"),
                        resultSet.getString("director"),
                        resultSet.getString("catalog_number"),
                        mediaType
                    );
                    items.add(media);
                }
            }
        }
        return items;
    }

    public static List<LibraryItem> searchByCatalogNumber(Connection connection, String catalogNumber) throws SQLException {
        List<LibraryItem> items = new ArrayList<>();
        
        String query = """
            SELECT item_id, title, director, catalog_number, type, is_available 
            FROM library_items 
            WHERE type = 'MEDIA' AND catalog_number LIKE ?""";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + catalogNumber + "%");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MediaTypeImpl mediaType = MediaTypeImpl.fromString(resultSet.getString("type"));
                    MediaItem media = new MediaItem(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getBoolean("is_available"),
                        resultSet.getString("director"),
                        resultSet.getString("catalog_number"),
                        mediaType
                    );
                    items.add(media);
                }
            }
        }
        return items;
    }

    public static boolean isItemAvailable(Connection connection, int mediaId) {
        String query = "SELECT is_available FROM library_items WHERE item_id = ? AND type = 'MEDIA'";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, mediaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("is_available");
                }
            }
        } catch (SQLException e) {
            logger.severe("Error checking media availability: " + e.getMessage());
        }
        return false;
    }

    public static boolean reserveMedia(Connection connection, int userId, int mediaId) {
        String query = "INSERT INTO reservations (user_id, item_id, reservation_date) VALUES (?, ?, CURRENT_DATE)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, mediaId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error reserving media: " + e.getMessage());
            return false;
        }
    }

    public static boolean borrowMedia(Connection connection, int userId, int mediaId) {
        if (!isItemAvailable(connection, mediaId)) {
            return false;
        }

        try {
            connection.setAutoCommit(false);
            
            try {
                // Mark item as unavailable
                String updateSql = "UPDATE library_items SET is_available = false WHERE item_id = ? AND type = 'MEDIA'";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, mediaId);
                    updateStmt.executeUpdate();
                }

                // Create loan record
                String loanSql = "INSERT INTO loans (user_id, item_id, loan_date) VALUES (?, ?, CURRENT_DATE)";
                try (PreparedStatement loanStmt = connection.prepareStatement(loanSql)) {
                    loanStmt.setInt(1, userId);
                    loanStmt.setInt(2, mediaId);
                    loanStmt.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                logger.severe("Transaction failed: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.severe("Error borrowing media: " + e.getMessage());
            return false;
        }
    }
}
