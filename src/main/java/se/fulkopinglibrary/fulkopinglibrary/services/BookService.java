package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.Book;
import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaItem;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaTypeImpl;
import se.fulkopinglibrary.fulkopinglibrary.utils.LoggerUtil;
import java.util.logging.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    // Search for books
    public static List<Book> searchBooks(Connection connection, String searchTerm, String searchType) {
        List<Book> books = new ArrayList<>();
        String query = """
            SELECT item_id, title, author, isbn, is_available 
            FROM library_items 
            WHERE type = ? AND title LIKE ?""";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + searchTerm + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Book book = new Book(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getString("isbn"),
                        resultSet.getBoolean("is_available")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Search for magazines
    public static List<Magazine> searchMagazines(Connection connection, String field, String searchTerm) {
        List<Magazine> magazines = new ArrayList<>();
        String query = """
            SELECT item_id, title, publisher, issn, is_available 
            FROM library_items 
            WHERE type = 'MAGAZINE' AND """ + field + " LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + searchTerm + "%");
            ResultSet resultSet = statement.executeQuery();

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return magazines;
    }

    private static final Logger logger = LoggerUtil.getLogger(BookService.class);

    // Helper method to get MediaTypeImpl from database
    private static MediaTypeImpl getMediaType(Connection connection, int mediaTypeId) {
        String query = "SELECT type_name, loan_period_days FROM media_types WHERE media_type_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, mediaTypeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new MediaTypeImpl(
                    rs.getString("type_name"),
                    rs.getInt("loan_period_days")
                );
            }
        } catch (SQLException e) {
            logger.severe("Error getting media type: " + e.getMessage());
        }
        return null;
    }

    // Search for library items
    public static List<LibraryItem> searchLibraryItems(Connection connection, String field, String searchTerm) {
        List<LibraryItem> items = new ArrayList<>();
        String query = "SELECT * FROM library_items WHERE " + field + " LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + searchTerm + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                LibraryItem item = null;
                
                switch (type) {
                    case "BOOK":
                        item = new Book(
                            resultSet.getInt("item_id"),
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getString("isbn"),
                            resultSet.getBoolean("is_available")
                        );
                        break;
                    case "MAGAZINE":
                        item = new Magazine(
                            resultSet.getInt("item_id"),
                            resultSet.getString("title"),
                            resultSet.getString("publisher"),
                            resultSet.getString("issn"),
                            resultSet.getBoolean("is_available")
                        );
                        break;
                    case "MEDIA":
                        item = new MediaItem(
                            resultSet.getInt("item_id"),
                            resultSet.getString("title"),
                            resultSet.getBoolean("is_available"),
                            resultSet.getString("director"),
                            resultSet.getString("catalog_number"),
                            getMediaType(connection, resultSet.getInt("media_type_id"))
                        );
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown media type: " + type);
                }
                
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static boolean borrowBook(Connection connection, int userId, int itemId) {
        String query = "INSERT INTO loans (user_id, item_id, loan_date) VALUES (?, ?, CURRENT_DATE)";
        String updateAvailability = "UPDATE library_items SET is_available = false WHERE item_id = ?";
        
        try {
            connection.setAutoCommit(false);
            
            // Check if item is available
            try (PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT is_available FROM library_items WHERE item_id = ?")) {
                checkStmt.setInt(1, itemId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || !rs.getBoolean("is_available")) {
                    return false;
                }
            }

            // Create loan
            try (PreparedStatement loanStmt = connection.prepareStatement(query)) {
                loanStmt.setInt(1, userId);
                loanStmt.setInt(2, itemId);
                loanStmt.executeUpdate();
            }

            // Update item availability
            try (PreparedStatement updateStmt = connection.prepareStatement(updateAvailability)) {
                updateStmt.setInt(1, itemId);
                updateStmt.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean returnBook(Connection connection, int loanId) {
        String query = "UPDATE loans SET return_date = CURRENT_DATE WHERE loan_id = ?";
        String updateAvailability = """
            UPDATE library_items SET is_available = true 
            WHERE item_id = (
                SELECT item_id FROM loans WHERE loan_id = ?
            )""";
        
        try {
            connection.setAutoCommit(false);
            
            // Update loan return date
            try (PreparedStatement loanStmt = connection.prepareStatement(query)) {
                loanStmt.setInt(1, loanId);
                loanStmt.executeUpdate();
            }

            // Update book availability
            try (PreparedStatement updateStmt = connection.prepareStatement(updateAvailability)) {
                updateStmt.setInt(1, loanId);
                updateStmt.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean reserveBook(Connection connection, int userId, int itemId) {
        String query = "INSERT INTO reservations (user_id, item_id, reservation_date) VALUES (?, ?, CURRENT_DATE)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<LibraryItem> viewLoanHistory(Connection connection, int userId) {
        List<LibraryItem> loans = new ArrayList<>();
        String query = """
            SELECT li.*, l.loan_date, l.return_date, mt.loan_period_days 
            FROM library_items li
            JOIN loans l ON li.item_id = l.item_id
            LEFT JOIN media_types mt ON li.media_type_id = mt.media_type_id
            WHERE l.user_id = ? AND l.return_date IS NOT NULL
            ORDER BY l.loan_date DESC""";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String type = rs.getString("type");
                LibraryItem item = null;
                
                switch (type) {
                    case "BOOK":
                        item = new Book(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getBoolean("is_available")
                        );
                        break;
                    case "MAGAZINE":
                        item = new Magazine(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getString("publisher"),
                            rs.getString("issn"),
                            rs.getBoolean("is_available")
                        );
                        break;
                    case "MEDIA":
                        item = new MediaItem(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getBoolean("is_available"),
                            rs.getString("director"),
                            rs.getString("catalog_number"),
                            getMediaType(connection, rs.getInt("media_type_id"))
                        );
                        break;
                }
                
                if (item != null) {
                    item.setLoanDate(rs.getDate("loan_date").toLocalDate());
                    item.setReturnDate(rs.getDate("return_date").toLocalDate());
                    loans.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public static List<LibraryItem> viewCurrentLoans(Connection connection, int userId) {
        List<LibraryItem> loans = new ArrayList<>();
        String query = """
            SELECT li.*, l.loan_date, mt.loan_period_days 
            FROM library_items li
            JOIN loans l ON li.item_id = l.item_id
            LEFT JOIN media_types mt ON li.media_type_id = mt.media_type_id
            WHERE l.user_id = ? AND l.return_date IS NULL
            ORDER BY l.loan_date DESC""";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String type = rs.getString("type");
                LibraryItem item = null;
                
                switch (type) {
                    case "BOOK":
                        item = new Book(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getBoolean("is_available")
                        );
                        break;
                    case "MAGAZINE":
                        item = new Magazine(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getString("publisher"),
                            rs.getString("issn"),
                            rs.getBoolean("is_available")
                        );
                        break;
                    case "MEDIA":
                        item = new MediaItem(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getBoolean("is_available"),
                            rs.getString("director"),
                            rs.getString("catalog_number"),
                            getMediaType(connection, rs.getInt("media_type_id"))
                        );
                        break;
                }
                
                if (item != null) {
                    item.setLoanDate(rs.getDate("loan_date").toLocalDate());
                    item.setLoanPeriodDays(rs.getInt("loan_period_days"));
                    loans.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }
}
