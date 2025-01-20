package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.Book;
import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;
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
            WHERE type = 'BOOK' AND title LIKE ?""";

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

    public static List<Book> viewLoanHistory(Connection connection, int userId) {
        List<Book> loans = new ArrayList<>();
        String query = """
            SELECT li.* FROM library_items li
            JOIN loans l ON li.item_id = l.item_id
            WHERE l.user_id = ? AND l.return_date IS NOT NULL
            AND li.type = 'BOOK'
            ORDER BY l.loan_date DESC""";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                loans.add(new Book(
                    rs.getInt("item_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getBoolean("is_available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public static List<Book> viewCurrentLoans(Connection connection, int userId) {
        List<Book> loans = new ArrayList<>();
        String query = """
            SELECT li.* FROM library_items li
            JOIN loans l ON li.item_id = l.item_id
            WHERE l.user_id = ? AND l.return_date IS NULL
            AND li.type = 'BOOK'
            ORDER BY l.loan_date DESC""";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                loans.add(new Book(
                    rs.getInt("item_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getBoolean("is_available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }
}
