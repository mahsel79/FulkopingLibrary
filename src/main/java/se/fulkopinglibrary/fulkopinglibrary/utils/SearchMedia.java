package se.fulkopinglibrary.fulkopinglibrary.utils;

import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;
import se.fulkopinglibrary.fulkopinglibrary.services.MediaService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class SearchMedia {
    public static void searchMedia(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("\n=== Search Media ===");
        System.out.println("1. Search by Title");
        System.out.println("2. Search by Director");
        System.out.println("3. Search by Catalog Number");
        System.out.println("4. Back to Search Menu");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        switch (choice) {
            case 1:
                System.out.print("Enter title: ");
                String title = scanner.nextLine();
                List<LibraryItem> byTitle = MediaService.searchByTitle(connection, title);
                displayResults(byTitle);
                break;
            case 2:
                System.out.print("Enter director: ");
                String director = scanner.nextLine();
                List<LibraryItem> byDirector = MediaService.searchByDirector(connection, director);
                displayResults(byDirector);
                break;
            case 3:
                System.out.print("Enter catalog number: ");
                String catalogNumber = scanner.nextLine();
                List<LibraryItem> byCatalog = MediaService.searchByCatalogNumber(connection, catalogNumber);
                displayResults(byCatalog);
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid option");
        }
    }

    private static void displayResults(List<LibraryItem> results) {
        if (results.isEmpty()) {
            System.out.println("No media found");
            return;
        }
        
        System.out.println("\nSearch Results:");
        for (LibraryItem item : results) {
            System.out.println("ID: " + item.getId());
            System.out.println("Title: " + item.getTitle());
            System.out.println("Available: " + (item.isAvailable() ? "Yes" : "No"));
            System.out.println();
        }
    }
}
