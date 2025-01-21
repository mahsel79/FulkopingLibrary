package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MagazineService {

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
