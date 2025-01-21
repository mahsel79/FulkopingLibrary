package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.MediaItem;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaTypeImpl;
import se.fulkopinglibrary.fulkopinglibrary.DatabaseConnection;
import se.fulkopinglibrary.fulkopinglibrary.utils.LoggerUtil;
import java.util.logging.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing media items in the library system.
 * Provides functionality for searching, borrowing, returning, and tracking media items.
 */
public class MediaService {
    private static final Logger logger = LoggerUtil.getLogger(MediaService.class);

    /**
     * Constructs a new MediaService.
     */
    public MediaService() {
    }

    /**
     * Searches for media items matching the given query.
     * 
     * @param query The search term to match against title, director, or catalog number
     * @return List of matching MediaItem objects
     * @throws SQLException if there is a database access error
     * @throws IllegalArgumentException if query is null or empty
     */
    public List<MediaItem> searchMedia(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            logger.warning("Search query cannot be null or empty");
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }

        List<MediaItem> results = new ArrayList<>();
        String sql = "SELECT * FROM library_items WHERE type = 'MEDIA' AND " +
                    "(title LIKE ? OR director LIKE ? OR catalog_number LIKE ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            logger.info("Executing media search with query: " + query);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MediaItem item = new MediaItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getBoolean("is_available"),
                        rs.getString("director"),
                        rs.getString("catalog_number"),
                        MediaTypeImpl.fromString(rs.getString("media_type"))
                    );
                    results.add(item);
                }
            }
            logger.info("Found " + results.size() + " matching media items");
        } catch (SQLException e) {
            logger.severe("Database error during media search: " + e.getMessage());
            throw e;
        }
        return results;
    }

    /**
     * Borrows a media item for a user.
     * 
     * @param mediaId The ID of the media item to borrow
     * @param userId The ID of the user borrowing the item
     * @return true if the item was successfully borrowed, false otherwise
     * @throws SQLException if there is a database access error
     * @throws IllegalArgumentException if mediaId or userId is invalid
     */
    public boolean borrowMedia(int mediaId, int userId) throws SQLException {
        if (mediaId <= 0 || userId <= 0) {
            logger.warning("Invalid mediaId or userId: mediaId=" + mediaId + ", userId=" + userId);
            throw new IllegalArgumentException("mediaId and userId must be positive integers");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Check if item exists and is available
            String checkSql = "SELECT id FROM library_items WHERE id = ? AND is_available = true FOR UPDATE";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, mediaId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        logger.info("Media item " + mediaId + " is not available for borrowing");
                        return false;
                    }
                }
            }

            // Borrow the item
            String borrowSql = "UPDATE library_items SET is_available = false, borrowed_by = ?, " +
                             "borrow_date = CURRENT_TIMESTAMP " +
                             "WHERE id = ?";
            try (PreparedStatement borrowStmt = conn.prepareStatement(borrowSql)) {
                borrowStmt.setInt(1, userId);
                borrowStmt.setInt(2, mediaId);
                
                int rowsUpdated = borrowStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    conn.commit();
                    logger.info("Successfully borrowed media item " + mediaId + " for user " + userId);
                    return true;
                }
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.severe("Error during transaction rollback: " + ex.getMessage());
                }
            }
            logger.severe("Database error during media borrowing: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Error resetting connection auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Returns a borrowed media item to the library.
     * 
     * @param mediaId The ID of the media item to return
     * @return true if the item was successfully returned, false otherwise
     * @throws SQLException if there is a database access error
     * @throws IllegalArgumentException if mediaId is invalid
     */
    public boolean returnMedia(int mediaId) throws SQLException {
        if (mediaId <= 0) {
            logger.warning("Invalid mediaId: " + mediaId);
            throw new IllegalArgumentException("mediaId must be a positive integer");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Check if item exists and is borrowed
            String checkSql = "SELECT id FROM library_items WHERE id = ? AND is_available = false FOR UPDATE";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, mediaId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        logger.info("Media item " + mediaId + " is not currently borrowed");
                        return false;
                    }
                }
            }

            // Return the item
            String returnSql = "UPDATE library_items SET is_available = true, borrowed_by = NULL, " +
                             "return_date = CURRENT_TIMESTAMP " +
                             "WHERE id = ?";
            try (PreparedStatement returnStmt = conn.prepareStatement(returnSql)) {
                returnStmt.setInt(1, mediaId);
                
                int rowsUpdated = returnStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    conn.commit();
                    logger.info("Successfully returned media item " + mediaId);
                    return true;
                }
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.severe("Error during transaction rollback: " + ex.getMessage());
                }
            }
            logger.severe("Database error during media return: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Error resetting connection auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves all media items currently borrowed by a user.
     * 
     * @param userId The ID of the user to get borrowed items for
     * @return List of MediaItem objects currently borrowed by the user
     * @throws SQLException if there is a database access error
     * @throws IllegalArgumentException if userId is invalid
     */
    public List<MediaItem> getBorrowedMedia(int userId) throws SQLException {
        if (userId <= 0) {
            logger.warning("Invalid userId: " + userId);
            throw new IllegalArgumentException("userId must be a positive integer");
        }

        List<MediaItem> results = new ArrayList<>();
        String sql = "SELECT * FROM library_items WHERE type = 'MEDIA' AND borrowed_by = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            logger.info("Fetching borrowed media for user " + userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MediaItem item = new MediaItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getBoolean("is_available"),
                        rs.getString("director"),
                        rs.getString("catalog_number"),
                        MediaTypeImpl.fromString(rs.getString("media_type"))
                    );
                    results.add(item);
                }
            }
            logger.info("Found " + results.size() + " borrowed media items for user " + userId);
        } catch (SQLException e) {
            logger.severe("Database error while fetching borrowed media: " + e.getMessage());
            throw e;
        }
        return results;
    }
}
