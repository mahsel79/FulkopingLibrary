package se.fulkopinglibrary.fulkopinglibrary.utils;

import se.fulkopinglibrary.fulkopinglibrary.services.BookService;
import se.fulkopinglibrary.fulkopinglibrary.models.Book;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class SearchUtils {

    // Public method to search for books
    public static void searchBooks(Connection connection, Scanner scanner) {
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine();
        System.out.print("Search by (title/author/isbn/general): ");
        String searchType = scanner.nextLine();

        // Call the BookService to search for books
        List<Book> books = BookService.searchBooks(connection, searchTerm, searchType);
        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("\nSearch Results:");
            for (Book book : books) {
                System.out.println("ID: " + book.getId() + ", Title: " + book.getTitle() +
                        ", Author: " + book.getAuthor() + ", Type: " + book.getType() +
                        ", ISBN: " + book.getIsbn() + // Display ISBN
                        ", Available: " + (book.isAvailable() ? "Yes" : "No"));
            }
        }
    }
}
