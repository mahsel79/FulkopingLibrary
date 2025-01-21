package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.MediaItem;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaTypeImpl;
import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaService {

    public static List<MediaItem> getAllItems(Connection connection) throws SQLException {
        return getAllItems(connection, 0, 1, 20);
    }

    public static List<MediaItem> getAllItems(Connection connection, int sortOption, int page, int pageSize) throws SQLException {
        List<MediaItem> items = new ArrayList<>();
        
        String orderBy = switch (sortOption) {
            case 1 -> "title ASC";
            case 2 -> "title DESC";
            case 3 -> "is_available DESC";
            default -> "item_id ASC";
        };
        
        String query = """
            SELECT item_id, title, is_available, director, catalog_number, media_type_id 
            FROM library_items 
            WHERE type = 'MEDIA'
            ORDER BY %s
            LIMIT ? OFFSET ?""".formatted(orderBy);
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int offset = (page - 1) * pageSize;
            statement.setInt(1, pageSize);
            statement.setInt(2, offset);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MediaItem media = new MediaItem(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getBoolean("is_available"),
                        resultSet.getString("director"),
                        resultSet.getString("catalog_number"),
                        getMediaType(connection, resultSet.getInt("media_type_id"))
                    );
                    items.add(media);
                }
            }
        }
        return items;
    }

    private static MediaTypeImpl getMediaType(Connection connection, int mediaTypeId) throws SQLException {
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
        }
        return null;
    }

    public static List<LibraryItem> searchByTitle(Connection connection, String title) throws SQLException {
        String query = "SELECT * FROM media WHERE title LIKE ?";
        return searchMedia(connection, query, "%" + title + "%");
    }

    public static List<LibraryItem> searchByDirector(Connection connection, String director) throws SQLException {
        String query = "SELECT * FROM media WHERE director LIKE ?";
        return searchMedia(connection, query, "%" + director + "%");
    }

    public static List<LibraryItem> searchByCatalogNumber(Connection connection, String catalogNumber) throws SQLException {
        String query = "SELECT * FROM media WHERE catalog_number = ?";
        return searchMedia(connection, query, catalogNumber);
    }

    public static List<LibraryItem> searchMedia(Connection connection, String query, String param) throws SQLException {
        List<LibraryItem> results = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, param);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MediaItem mediaItem = new MediaItem(
                        resultSet.getInt("item_id"),
                        resultSet.getString("title"),
                        resultSet.getBoolean("is_available"),
                        resultSet.getString("director"),
                        resultSet.getString("catalog_number"),
                        getMediaType(connection, resultSet.getInt("media_type_id"))
                    );
                    results.add(mediaItem);
                }
            }
        }
        return results;
    }
}
