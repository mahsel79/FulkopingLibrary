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
import java.util.Set;

public class BookService {

    // Search for books
    public static List<Book> searchBooks(Connection connection, String searchTerm, String searchType, int sortOption, int page, int pageSize) throws SQLException {
        // Validate searchType parameter
        if (searchType == null || !Set.of("title", "author", "isbn", "general").contains(searchType)) {
            throw new IllegalArgumentException("Invalid search type. Must be one of: title, author, isbn, general");
        }

        List<Book> books = new ArrayList<>();
        String query;
        
        // Base query with sorting
        String orderBy = switch (sortOption) {
            case 1 -> "title ASC";
            case 2 -> "title DESC";
            case 3 -> "is_available DESC";
            default -> "item_id ASC";
        };
        
        switch (searchType) {
            case "title":
                query = """
                    SELECT item_id, title, author, isbn, is_available 
                    FROM library_items 
                    WHERE type = ? AND title LIKE ?
                    ORDER BY %s
                    LIMIT ? OFFSET ?""".formatted(orderBy);
                break;
            case "author":
                query = """
                    SELECT item_id, title, author, isbn, is_available 
                    FROM library_items 
                    WHERE type = ? AND author LIKE ?
                    ORDER BY %s
                    LIMIT ? OFFSET ?""".formatted(orderBy);
                break;
            case "isbn":
                query = """
                    SELECT item_id, title, author, isbn, is_available 
                    FROM library_items 
                    WHERE type = ? AND isbn = ?
                    ORDER BY %s
                    LIMIT ? OFFSET ?""".formatted(orderBy);
                break;
            case "general":
                query = """
                    SELECT item_id, title, author, isbn, is_available 
                    FROM library_items 
                    WHERE type = ? AND (title LIKE ? OR author LIKE ? OR isbn LIKE ?)
                    ORDER BY %s
                    LIMIT ? OFFSET ?""".formatted(orderBy);
                break;
            default:
                // This case should never be reached due to the validation above
                throw new IllegalStateException("Unexpected search type: " + searchType);
        }

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // Set parameters based on search type
            statement.setString(1, "BOOK");
            
            if (searchType.equals("general")) {
                String likeTerm = "%" + searchTerm + "%";
                statement.setString(2, likeTerm);
                statement.setString(3, likeTerm);
                statement.setString(4, likeTerm);
            } else {
                // For title, author, and isbn searches
                if (searchType.equals("isbn")) {
                    statement.setString(2, searchTerm);
                } else {
                    statement.setString(2, "%" + searchTerm + "%");
                }
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
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
            }
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
    public static List<LibraryItem> searchLibraryItems(Connection connection, String field, String searchTerm) throws SQLException {
        // Validate search type parameter
        if (field == null || !Set.of("title", "author", "isbn", "general").contains(field)) {
            throw new IllegalArgumentException("Invalid search type. Must be one of: title, author, isbn, general");
        }

        List<LibraryItem> items = new ArrayList<>();
        String query;
        
        switch (field) {
            case "title":
                query = "SELECT * FROM library_items WHERE title LIKE ?";
                break;
            case "author":
                query = "SELECT * FROM library_items WHERE author LIKE ?";
                break;
            case "isbn":
                query = "SELECT * FROM library_items WHERE isbn = ?";
                break;
            case "general":
                query = """
                    SELECT * FROM library_items 
                    WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?""";
                break;
            default:
                // This case should never be reached due to the validation above
                throw new IllegalStateException("Unexpected search type: " + field);
        }

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            switch (field) {
                case "title":
                case "author":
                    statement.setString(1, "%" + searchTerm + "%");
                    break;
                case "isbn":
                    statement.setString(1, searchTerm);
                    break;
                case "general":
                    String likeTerm = "%" + searchTerm + "%";
                    statement.setString(1, likeTerm);
                    statement.setString(2, likeTerm);
                    statement.setString(3, likeTerm);
                    break;
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
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
            }
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

    public static List<LibraryItem> getAllItems(Connection connection) throws SQLException {
        List<LibraryItem> items = new ArrayList<>();
        String query = "SELECT item_id, title, author, isbn, is_available FROM library_items WHERE type = 'BOOK'";
        
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Book book = new Book(
                    resultSet.getInt("item_id"),
                    resultSet.getString("title"),
                    resultSet.getString("author"),
                    resultSet.getString("isbn"),
                    resultSet.getBoolean("is_available")
                );
                items.add(book);
            }
        }
        return items;
    }
}
