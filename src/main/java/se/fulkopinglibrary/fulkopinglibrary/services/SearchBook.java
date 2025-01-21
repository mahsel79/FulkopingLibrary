package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.Book;
import java.util.logging.Logger;
import se.fulkopinglibrary.fulkopinglibrary.utils.Searchable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SearchBook implements Searchable<Book> {
    private static final Logger logger = Logger.getLogger(SearchBook.class.getName());

    @Override
    public List<Book> search(Connection connection, String searchTerm, String searchType, int sortOption, int page, int pageSize) {
        try {
            return BookService.searchBooks(connection, searchTerm, searchType, sortOption, page, pageSize);
        } catch (SQLException e) {
            logger.severe("Error searching books: " + e.getMessage());
            throw new RuntimeException("Database error during search", e);
        }
    }

    @Override
    public String getDisplayHeader() {
        return "=== Books ===\n" +
               "ID | Title | Author | ISBN | Available\n" +
               "----------------------------------------";
    }

    @Override
    public String getDisplayRow(Book book) {
        return String.format("%-4d | %-30s | %-20s | %-13s | %-8s",
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.isAvailable() ? "Yes" : "No");
    }
}
