package se.fulkopinglibrary.fulkopinglibrary.utils;

import se.fulkopinglibrary.fulkopinglibrary.services.BookService;
import se.fulkopinglibrary.fulkopinglibrary.services.MediaService;
import se.fulkopinglibrary.fulkopinglibrary.services.MagazineService;
import se.fulkopinglibrary.fulkopinglibrary.models.Book;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaItem;
import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class SearchUtils {

    // Search for books
    public static void searchBooks(Connection connection, Scanner scanner) {
        try {
            while (true) {
                System.out.println("\n=== Book Search Menu ===");
                System.out.println("=== Search Options ===");
                System.out.println("1. Search Title");
                System.out.println("2. Search Author");
                System.out.println("3. Search ISBN");
                System.out.println("4. General Search");
                System.out.println("5. Back to Main Menu");
                System.out.print("Choose an option (1-5): ");

                int searchChoice = getValidChoice(scanner, 1, 5);
                if (searchChoice == 5) return;

                String searchType = switch (searchChoice) {
                    case 1 -> "title";
                    case 2 -> "author";
                    case 3 -> "isbn";
                    case 4 -> "general";
                    default -> throw new IllegalStateException("Unexpected value: " + searchChoice);
                };

                System.out.println("\n=== Search Options ===");
                System.out.println("1. Enter search term");
                System.out.println("2. Back to search menu");
                System.out.print("Choose an option (1-2): ");
                
                int termChoice = getValidChoice(scanner, 1, 2);
                if (termChoice == 2) continue;

                System.out.print("\nEnter search term: ");
                String searchTerm = scanner.nextLine().trim();
                
                if (searchTerm.isEmpty()) {
                    System.out.println("Search term cannot be empty. Please try again.");
                    continue;
                }

                performBookSearch(connection, searchTerm, searchType);
            }
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Utility method to get a valid menu choice
    public static int getValidChoice(Scanner scanner, int min, int max) {
        int choice = -1;
        while (choice < min || choice > max) {
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < min || choice > max) {
                    System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
            }
        }
        return choice;
    }

    // Utility method to get a valid search term
    public static String getSearchTerm(Scanner scanner) {
        String searchTerm = "";
        while (searchTerm.trim().isEmpty()) {
            System.out.print("Enter search term (or 'back' to return): ");
            searchTerm = scanner.nextLine();
            if (searchTerm.trim().isEmpty()) {
                System.out.println("Search term cannot be empty. Please try again.");
            }
        }
        return searchTerm;
    }

    // Perform book search
    private static void performBookSearch(Connection connection, String searchTerm, String searchType) {
        try {
            List<Book> books = BookService.searchBooks(connection, searchTerm, searchType, 0, 1, 20);
            displayResults(books, "book");
        } catch (Exception e) {
            System.out.println("\nAn error occurred during search: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    // Search for media
    public static void searchMedia(Connection connection, Scanner scanner) {
        while (true) {
            System.out.println("\n=== Media Search Menu ===");
            System.out.println("1. Search by Title");
            System.out.println("2. Search by Director");
            System.out.println("3. Search by Catalog Number");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option (1-4): ");

            int searchChoice = getValidChoice(scanner, 1, 4);
            if (searchChoice == 4) return;

            String searchType = switch (searchChoice) {
                case 1 -> "title";
                case 2 -> "director";
                case 3 -> "catalog_number";
                default -> throw new IllegalStateException("Unexpected value: " + searchChoice);
            };

            String searchTerm = getSearchTerm(scanner);
            if (searchTerm.equalsIgnoreCase("back")) continue;

            performMediaSearch(connection, searchTerm, searchType);
        }
    }

    // Perform media search
    private static void performMediaSearch(Connection connection, String searchTerm, String searchType) {
        try {
            List<LibraryItem> filteredMedia = MediaService.getAllItems(connection).stream()
                .filter(item -> {
                    if (!(item instanceof MediaItem media)) {
                        return false;
                    }
                    switch (searchType) {
                        case "title":
                            return media.getTitle().toLowerCase().contains(searchTerm.toLowerCase());
                        case "director":
                            return media.getDirector().toLowerCase().contains(searchTerm.toLowerCase());
                        case "catalog_number":
                            return media.getCatalogNumber().toLowerCase().contains(searchTerm.toLowerCase());
                        default:
                            return true;
                    }
                })
                .toList();
            displayResults(filteredMedia, "media");
        } catch (Exception e) {
            System.out.println("\nAn error occurred during search: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    // Search for magazines
    public static void searchMagazines(Connection connection, Scanner scanner) {
        while (true) {
            System.out.println("\n=== Magazine Search Menu ===");
            System.out.println("1. Search by Title");
            System.out.println("2. Search by Publisher");
            System.out.println("3. Search by ISSN");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option (1-4): ");

            int searchChoice = getValidChoice(scanner, 1, 4);
            if (searchChoice == 4) return;

            String searchType = switch (searchChoice) {
                case 1 -> "title";
                case 2 -> "publisher";
                case 3 -> "issn";
                default -> throw new IllegalStateException("Unexpected value: " + searchChoice);
            };

            String searchTerm = getSearchTerm(scanner);
            if (searchTerm.equalsIgnoreCase("back")) continue;

            performMagazineSearch(connection, searchTerm, searchType);
        }
    }

    // Perform magazine search
    private static void performMagazineSearch(Connection connection, String searchTerm, String searchType) {
        try {
            List<Magazine> allMagazines = MagazineService.getAllItems(connection);
            List<Magazine> filteredMagazines = allMagazines.stream()
                .filter(magazine -> {
                    switch (searchType) {
                        case "title":
                            return magazine.getTitle().toLowerCase().contains(searchTerm.toLowerCase());
                        case "publisher":
                            return magazine.getPublisher().toLowerCase().contains(searchTerm.toLowerCase());
                        case "issn":
                            return magazine.getIssn().toLowerCase().contains(searchTerm.toLowerCase());
                        default:
                            return true;
                    }
                })
                .toList();
            displayResults(filteredMagazines, "magazine");
        } catch (Exception e) {
            System.out.println("\nAn error occurred during search: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    // Explore all items with pagination and sorting
    public static void exploreMenu(Connection connection, Scanner scanner) {
        while (true) {
            System.out.println("\n=== Explore Menu ===");
            System.out.println("1. Explore Books");
            System.out.println("2. Explore Media");
            System.out.println("3. Explore Magazines");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option (1-4): ");

            int choice = getValidChoice(scanner, 1, 4);
            if (choice == 4) return;

            // Get sorting preference
            System.out.println("\n=== Sort Options ===");
            System.out.println("1. By Title (A-Z)");
            System.out.println("2. By Title (Z-A)");
            System.out.println("3. By Availability");
            System.out.println("4. No Sorting");
            System.out.print("Choose sort option (1-4): ");
            int sortChoice = getValidChoice(scanner, 1, 4);

            // Get page size
            System.out.print("\nEnter number of items per page (10-100): ");
            int pageSize = getValidChoice(scanner, 10, 100);

            int currentPage = 1;
            boolean hasMore = true;
            
            while (hasMore) {
                try {
                    List<? extends LibraryItem> items = switch (choice) {
                        case 1 -> {
                            yield BookService.searchBooks(connection, "", "general", sortChoice, currentPage, pageSize);
                        }
                        case 2 -> MediaService.getAllItems(connection, sortChoice, currentPage, pageSize);
                        case 3 -> MagazineService.getAllItems(connection, sortChoice, currentPage, pageSize);
                        default -> Collections.emptyList();
                    };

                    if (items.isEmpty()) {
                        System.out.println("\nNo more items found.");
                        break;
                    }

                    displayResults(items, choice == 1 ? "book" : choice == 2 ? "media" : "magazine");

                    // Pagination controls
                    System.out.println("\n=== Page " + currentPage + " ===");
                    System.out.println("1. Next Page");
                    System.out.println("2. Previous Page");
                    System.out.println("3. New Search");
                    System.out.println("4. Back to Explore Menu");
                    System.out.print("Choose option (1-4): ");
                    
                    int pageChoice = getValidChoice(scanner, 1, 4);
                    switch (pageChoice) {
                        case 1 -> currentPage++;
                        case 2 -> currentPage = Math.max(1, currentPage - 1);
                        case 3 -> { hasMore = false; currentPage = 1; }
                        case 4 -> { return; }
                    }
                } catch (Exception e) {
                    System.out.println("\nAn error occurred: " + e.getMessage());
                    System.out.println("Please try again.");
                    break;
                }
            }
        }
    }

    // Display search results
    private static <T extends LibraryItem> void displayResults(List<T> items, String type) {
        if (items.isEmpty()) {
            System.out.println("\nNo " + type + "s found matching your search.");
            return;
        }

        System.out.println("\n=== Search Results ===");
        if (type.equals("book")) {
            System.out.printf("%-5s %-40s %-25s %-15s %-10s\n",
                    "ID", "Title", "Author", "ISBN", "Available");
        } else if (type.equals("media")) {
            System.out.printf("%-5s %-40s %-25s %-15s %-10s\n",
                    "ID", "Title", "Director", "Catalog No.", "Available");
        } else if (type.equals("magazine")) {
            System.out.printf("%-5s %-40s %-25s %-15s %-10s\n",
                    "ID", "Title", "Publisher", "ISSN", "Available");
        }
        System.out.println("---------------------------------------------------------------");

        for (T item : items) {
            if (item instanceof Book book) {
                System.out.printf("%-5d %-40s %-25s %-15s %-10s\n",
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.isAvailable() ? "Yes" : "No");
            } else if (item instanceof MediaItem media) {
                System.out.printf("%-5d %-40s %-25s %-15s %-10s\n",
                        media.getId(),
                        media.getTitle(),
                        media.getDirector(),
                        media.getCatalogNumber(),
                        media.isAvailable() ? "Yes" : "No");
            } else if (item instanceof Magazine magazine) {
                System.out.printf("%-5d %-40s %-25s %-15s %-10s\n",
                        magazine.getId(),
                        magazine.getTitle(),
                        magazine.getPublisher(),
                        magazine.getIssn(),
                        magazine.isAvailable() ? "Yes" : "No");
            }
        }
    }
}
