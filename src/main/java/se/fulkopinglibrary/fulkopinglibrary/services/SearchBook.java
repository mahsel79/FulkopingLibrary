package se.fulkopinglibrary.fulkopinglibrary.services;

import se.fulkopinglibrary.fulkopinglibrary.models.Book;
import java.sql.SQLException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SearchBook {
    public static List<Book> getAllMagazines(Connection connection) throws SQLException {
        return BookService.searchBooks(connection, "", "magazine");
    }

    public static void searchBooks(Connection connection, Scanner scanner) {
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine();
        System.out.println("Search by:");
        System.out.println("1. Title");
        System.out.println("2. Author"); 
        System.out.println("3. ISBN");
        System.out.println("4. General");
        System.out.print("Choose an option (1-4): ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        String searchType;
        switch (choice) {
            case 1:
                searchType = "title";
                break;
            case 2:
                searchType = "author";
                break;
            case 3:
                searchType = "isbn";
                break;
            case 4:
                searchType = "general";
                break;
            default:
                System.out.println("Invalid choice, using general search");
                searchType = "general";
        }

        List<Book> books;
        try {
            books = BookService.searchBooks(connection, searchTerm, searchType);
        } catch (SQLException e) {
            System.out.println("Error searching books: " + e.getMessage());
            return;
        }
        if (books.isEmpty()) {
            System.out.println("\nNo items found.");
            return;
        }

        // Separate books, magazines, and media
        List<Book> booksList = new ArrayList<>();
        List<Book> magazinesList = new ArrayList<>();
        List<Book> mediaList = new ArrayList<>();

        for (Book book : books) {
            switch (book.getType().toString()) {
                case "book":
                    booksList.add(book);
                    break;
                case "magazine":
                    magazinesList.add(book);
                    break;
                case "media":
                    mediaList.add(book);
                    break;
            }
        }

        // Display books
        if (!booksList.isEmpty()) {
            System.out.println("\n=== Books ===");
            for (Book book : booksList) {
                System.out.println("----------------------------------------");
                System.out.println("ID: " + book.getId());
                System.out.println("Title: " + book.getTitle());
                System.out.println("Author: " + book.getAuthor());
                System.out.println("ISBN: " + book.getIsbn());
                System.out.println("Available: " + (book.isAvailable() ? "Yes" : "No"));
                System.out.println("----------------------------------------");
            }
        }

        // Display magazines
        if (!magazinesList.isEmpty()) {
            System.out.println("\n=== Magazines ===");
            for (Book magazine : magazinesList) {
                System.out.println("----------------------------------------");
                System.out.println("ID: " + magazine.getId());
                System.out.println("Title: " + magazine.getTitle());
                System.out.println("Publisher: " + magazine.getAuthor());
                System.out.println("ISSN: " + magazine.getIsbn());
                System.out.println("Available: " + (magazine.isAvailable() ? "Yes" : "No"));
                System.out.println("----------------------------------------");
            }
        }

        // Display media
        if (!mediaList.isEmpty()) {
            System.out.println("\n=== Media ===");
            for (Book media : mediaList) {
                System.out.println("----------------------------------------");
                System.out.println("ID: " + media.getId());
                System.out.println("Title: " + media.getTitle());
                System.out.println("Director: " + media.getAuthor());
                System.out.println("Catalog Number: " + media.getIsbn());
                System.out.println("Available: " + (media.isAvailable() ? "Yes" : "No"));
                System.out.println("----------------------------------------");
            }
        }
    }
}
