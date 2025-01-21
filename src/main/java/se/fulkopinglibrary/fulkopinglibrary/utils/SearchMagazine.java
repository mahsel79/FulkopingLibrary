package se.fulkopinglibrary.fulkopinglibrary.utils;

import se.fulkopinglibrary.fulkopinglibrary.services.MagazineService;
import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import se.fulkopinglibrary.fulkopinglibrary.DatabaseConnection;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class SearchMagazine {

    public static void searchMagazines(Scanner scanner) {
        while (true) {
            System.out.println("\n=== Magazine Search Menu ===");
            System.out.println("1. Search by Title");
            System.out.println("2. Search by Publisher");
            System.out.println("3. Search by ISSN");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option (1-4): ");

            int searchChoice = SearchUtils.getValidChoice(scanner, 1, 4);
            if (searchChoice == 4) return;

            String searchType = switch (searchChoice) {
                case 1 -> "title";
                case 2 -> "publisher";
                case 3 -> "issn";
                default -> throw new IllegalStateException("Unexpected value: " + searchChoice);
            };

            String searchTerm = SearchUtils.getSearchTerm(scanner);
            if (searchTerm.equalsIgnoreCase("back")) continue;

            performMagazineSearch(searchTerm, searchType);
        }
    }

    private static void performMagazineSearch(String searchTerm, String searchType) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            List<Magazine> magazines = MagazineService.searchMagazines(connection, searchType, searchTerm);
            displayResults(magazines);
        } catch (Exception e) {
            System.out.println("\nAn error occurred during search: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    private static void displayResults(List<Magazine> magazines) {
        if (magazines.isEmpty()) {
            System.out.println("\nNo magazines found matching your search.");
            return;
        }

        System.out.println("\n=== Search Results ===");
        System.out.printf("%-5s %-40s %-25s %-15s %-10s\n",
                "ID", "Title", "Publisher", "ISSN", "Available");
        System.out.println("---------------------------------------------------------------");

        for (Magazine magazine : magazines) {
            System.out.printf("%-5d %-40s %-25s %-15s %-10s\n",
                    magazine.getId(),
                    magazine.getTitle(),
                    magazine.getPublisher(),
                    magazine.getIssn(),
                    magazine.isAvailable() ? "Yes" : "No");
        }
    }
}
