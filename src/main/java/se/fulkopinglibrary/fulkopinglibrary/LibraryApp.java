/**
 * The main application class for Fulköping Library system.
 * 
 * <p>This class provides the entry point for the library management system,
 * handling user interactions through a console-based menu system. It manages
 * all core library operations including user authentication, item search,
 * borrowing, returning, and reservation of library items.</p>
 *
 * @author Library Development Team
 * @version 1.0
 */
package se.fulkopinglibrary.fulkopinglibrary;

import se.fulkopinglibrary.fulkopinglibrary.models.User;
import se.fulkopinglibrary.fulkopinglibrary.services.UserService;
import se.fulkopinglibrary.fulkopinglibrary.services.BookService;
import se.fulkopinglibrary.fulkopinglibrary.services.MagazineService;
import se.fulkopinglibrary.fulkopinglibrary.services.MediaService;
import se.fulkopinglibrary.fulkopinglibrary.utils.SearchUtils;
import se.fulkopinglibrary.fulkopinglibrary.utils.SearchMagazine;
import se.fulkopinglibrary.fulkopinglibrary.models.Book;
import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;
import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaItem;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import se.fulkopinglibrary.fulkopinglibrary.utils.LoggerUtil;

public class LibraryApp {
    private static final Logger logger = LoggerUtil.getLogger(LibraryApp.class);

    public static void main(String[] args) {
        testDatabaseConnection();
        try {
            logger.info("Initializing library application...");
            Scanner scanner = new Scanner(System.in);
            boolean running = true;
            logger.info("Application initialized successfully");

            while (running) {
                logger.fine("Main menu loop started");
                System.out.println("\n=== Welcome to Fulköping Library ===");
                System.out.println("1. Login");
                System.out.println("2. Signup");
                System.out.println("3. Search");
                System.out.println("4. Explore");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            logger.info("Attempting login...");
                            long startTime = System.currentTimeMillis();
                            User user = UserService.login(connection, scanner);
                            long duration = System.currentTimeMillis() - startTime;
                            
                            if (user != null) {
                                logger.info(String.format("Login successful! (took %d ms)", duration));
                                userMenu(user, scanner);
                            } else {
                                logger.warning(String.format("Login failed after %d ms. Invalid username or password.", duration));
                            }
                        } catch (SQLException e) {
                            logger.severe("Login failed with database error: " + e.getMessage());
                        }
                        break;
                    case 2:
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            logger.info("Attempting signup...");
                            long startTime = System.currentTimeMillis();
                            boolean signupSuccess = UserService.signup(connection, scanner);
                            long duration = System.currentTimeMillis() - startTime;
                            
                            if (signupSuccess) {
                                logger.info(String.format("Signup successful! (took %d ms) Please log in.", duration));
                            } else {
                                logger.warning(String.format("Signup failed after %d ms. Please try again.", duration));
                            }
                        } catch (SQLException e) {
                            logger.severe("Signup failed with database error: " + e.getMessage());
                        }
                        break;
                    case 3:
                        logger.info("Opening search menu...");
                        searchMenu(scanner);
                        break;
                    case 4:
                        logger.info("Opening explore menu...");
                        exploreMenu(scanner);
                        break;
                    case 5:
                        running = false;
                        logger.info("Exiting the system. Goodbye!");
                        DatabaseConnection.closePool();
                        break;
                    default:
                        logger.warning("Invalid option selected");
                        System.out.println("Invalid option. Try again.");
    }
}
        } catch (Exception e) {
            logger.severe("An unexpected error occurred: " + e.getMessage());
        } finally {
            DatabaseConnection.closePool();
        }
    }


    private static void displayItems(String category, List<LibraryItem> items) {
        if (items.isEmpty()) {
            System.out.println("\nNo " + category.toLowerCase() + " found.");
            return;
        }
        
        System.out.println("\n" + category + ":");
        for (LibraryItem item : items) {
            System.out.println("ID: " + item.getId() + 
                ", Title: " + item.getTitle() +
                ", Available: " + (item.isAvailable() ? "Yes" : "No"));
            
            if (item instanceof Book) {
                Book book = (Book) item;
                System.out.println("  Author: " + book.getAuthor() + 
                    ", ISBN: " + book.getIsbn());
            } else if (item instanceof Magazine) {
                Magazine magazine = (Magazine) item;
                System.out.println("  Publisher: " + magazine.getPublisher() + 
                    ", ISSN: " + magazine.getIssn());
            } else if (item instanceof MediaItem) {
                MediaItem media = (MediaItem) item;
                System.out.println("  Director: " + media.getDirector() + 
                    ", Catalog Number: " + media.getCatalogNumber());
            }
        }
    }

    private static void testDatabaseConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            logger.info("Successfully connected to database!");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    logger.info("Database test query successful: " + rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.severe("Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void userMenu(User user, Scanner scanner) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1. Search for Books");
            System.out.println("2. Borrow a Book");
            System.out.println("3. Return a Book");
            System.out.println("4. Reserve a Book");
            System.out.println("5. View Loan History");
            System.out.println("6. View Current Loans");
            System.out.println("7. Update Profile");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            try (Connection connection = DatabaseConnection.getConnection()) {
                logger.info("Database connection established for user menu");
                long startTime = System.currentTimeMillis();
                
                switch (choice) {
                    case 1:
                        SearchUtils.searchBooks(connection, scanner);
                        break;
                    case 2:
                        borrowBook(connection, user.getUserId(), scanner);
                        break;
                    case 3:
                        returnBook(connection, user.getUserId(), scanner);
                        break;
                    case 4:
                        reserveBook(connection, user.getUserId(), scanner);
                        break;
                    case 5:
                        viewLoanHistory(connection, user.getUserId());
                        break;
                    case 6:
                        viewCurrentLoans(connection, user.getUserId());
                        break;
                    case 7:
                        updateProfile(connection, user.getUserId(), scanner);
                        break;
                    case 8:
                        loggedIn = false;
                        logger.info("User logged out successfully");
                        System.out.println("Logged out successfully.");
                        break;
                    default:
                        logger.warning("Invalid option selected in user menu");
                        System.out.println("Invalid option. Try again.");
                }
                
                logger.info(String.format("User menu operation completed in %d ms", System.currentTimeMillis() - startTime));
            } catch (SQLException e) {
                logger.severe("Database connection error in user menu: " + e.getMessage());
            }
        }
    }

    private static void borrowBook(Connection connection, int userId, Scanner scanner) {
        System.out.print("Enter the ID of the book you want to borrow: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        boolean success = BookService.borrowBook(connection, userId, bookId);
        if (success) {
            logger.info("Book borrowed successfully: user=" + userId + ", book=" + bookId);
            System.out.println("Book borrowed successfully!");
        } else {
            logger.warning("Failed to borrow book: user=" + userId + ", book=" + bookId);
            System.out.println("Failed to borrow the book. It may not be available.");
        }
    }

    private static void returnBook(Connection connection, int userId, Scanner scanner) {
        System.out.print("Enter the ID of the loan you want to return: ");
        int loanId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        boolean success = BookService.returnBook(connection, loanId);
        if (success) {
            logger.info("Book returned successfully: loan=" + loanId);
            System.out.println("Book returned successfully!");
        } else {
            logger.warning("Failed to return book: loan=" + loanId);
            System.out.println("Failed to return the book. Please check the loan ID.");
        }
    }

    private static void reserveBook(Connection connection, int userId, Scanner scanner) {
        System.out.print("Enter the ID of the book you want to reserve: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        boolean success = BookService.reserveBook(connection, userId, bookId);
        if (success) {
            logger.info("Book reserved successfully: user=" + userId + ", book=" + bookId);
            System.out.println("Book reserved successfully!");
        } else {
            logger.warning("Failed to reserve book: user=" + userId + ", book=" + bookId);
            System.out.println("Failed to reserve the book. It may already be available.");
        }
    }

    private static void viewLoanHistory(Connection connection, int userId) {
        List<LibraryItem> loans = BookService.viewLoanHistory(connection, userId);
        if (loans.isEmpty()) {
            logger.info("No loan history found for user: " + userId);
            System.out.println("No loan history found.");
        } else {
            logger.info("Displaying loan history for user: " + userId);
            System.out.println("\nLoan History:");
            for (LibraryItem loan : loans) {
                System.out.println("ID: " + loan.getId() + ", Title: " + loan.getTitle() +
                        ", Type: " + loan.getType() + ", Available: " + (loan.isAvailable() ? "Yes" : "No"));
                
                if (loan instanceof Book) {
                    Book book = (Book) loan;
                    System.out.println("  Author: " + book.getAuthor() + ", ISBN: " + book.getIsbn());
                } else if (loan instanceof Magazine) {
                    Magazine magazine = (Magazine) loan;
                    System.out.println("  Publisher: " + magazine.getPublisher() + ", ISSN: " + magazine.getIssn());
                } else if (loan instanceof MediaItem) {
                    MediaItem media = (MediaItem) loan;
                    System.out.println("  Director: " + media.getDirector() + ", Catalog Number: " + media.getCatalogNumber());
                }
                
                System.out.println("  Loan Date: " + loan.getLoanDate() + 
                    ", Return Date: " + loan.getReturnDate());
            }
        }
    }

    private static void viewCurrentLoans(Connection connection, int userId) {
        List<LibraryItem> loans = BookService.viewCurrentLoans(connection, userId);
        if (loans.isEmpty()) {
            logger.info("No current loans found for user: " + userId);
            System.out.println("No current loans found.");
        } else {
            logger.info("Displaying current loans for user: " + userId);
            System.out.println("\nCurrent Loans:");
            for (LibraryItem loan : loans) {
                System.out.println("ID: " + loan.getId() + ", Title: " + loan.getTitle() +
                        ", Type: " + loan.getType() + ", Available: " + (loan.isAvailable() ? "Yes" : "No"));
                
                if (loan instanceof Book) {
                    Book book = (Book) loan;
                    System.out.println("  Author: " + book.getAuthor() + ", ISBN: " + book.getIsbn());
                } else if (loan instanceof Magazine) {
                    Magazine magazine = (Magazine) loan;
                    System.out.println("  Publisher: " + magazine.getPublisher() + ", ISSN: " + magazine.getIssn());
                } else if (loan instanceof MediaItem) {
                    MediaItem media = (MediaItem) loan;
                    System.out.println("  Director: " + media.getDirector() + ", Catalog Number: " + media.getCatalogNumber());
                }
                
                System.out.println("  Loan Date: " + loan.getLoanDate() + 
                    ", Due Date: " + loan.getLoanDate().plusDays(loan.getLoanPeriodDays()));
            }
        }
    }

    private static void updateProfile(Connection connection, int userId, Scanner scanner) {
        boolean success = UserService.updateProfile(connection, userId, scanner);
        if (success) {
            System.out.println("Profile updated successfully!");
        } else {
            System.out.println("Failed to update profile.");
        }
    }

    private static void searchMenu(Scanner scanner) {
        boolean searching = true;
        
        while (searching) {
            System.out.println("\n=== Search Menu ===");
            System.out.println("1. Search Books");
            System.out.println("2. Search Magazines");
            System.out.println("3. Search Media");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option (1-4): ");
            
            int choice = -1;
            while (choice < 1 || choice > 4) {
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                    if (choice < 1 || choice > 4) {
                        System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number between 1 and 4.");
                }
            }
            
            try (Connection connection = DatabaseConnection.getConnection()) {
                long startTime = System.currentTimeMillis();
                
                switch (choice) {
                    case 1:
                        logger.info("Searching books...");
                        System.out.println("\nSearch Books By:");
                        System.out.println("1. Title");
                        System.out.println("2. Author");
                        System.out.println("3. ISBN");
                        System.out.println("4. General Search");
                        System.out.print("Choose search type: ");
                        int searchType = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        System.out.print("Enter search term: ");
                        String bookSearchTerm = scanner.nextLine();
                        
                        String field = "";
                        switch (searchType) {
                            case 1:
                                field = "title";
                                break;
                            case 2:
                                field = "author";
                                break;
                            case 3:
                                field = "isbn";
                                break;
                            case 4:
                                field = "general";
                                break;
                            default:
                                System.out.println("Invalid search type");
                                continue;
                        }
                        
                        List<LibraryItem> books = BookService.searchLibraryItems(connection, field, bookSearchTerm);
                        displayItems("Books", books);
                        break;
                    case 2:
                        logger.info("Searching magazines...");
                        System.out.print("Enter search term: ");
                        String magazineSearchTerm = scanner.nextLine();
                        try {
                            List<Magazine> magazines = MagazineService.searchMagazines(connection, "title", magazineSearchTerm);
                            displayItems("Magazines", new ArrayList<>(magazines));
                        } catch (SQLException e) {
                            logger.severe("Magazine search failed: " + e.getMessage());
                            System.out.println("An error occurred while searching magazines. Please try again.");
                        }
                        break;
                    case 3:
                        logger.info("Searching media...");
                        System.out.print("Enter search term: ");
                        String mediaSearchTerm = scanner.nextLine();
                        try {
                            List<LibraryItem> media = MediaService.searchMedia(connection, "title", mediaSearchTerm);
                            displayItems("Media", media);
                        } catch (SQLException e) {
                            logger.severe("Media search failed: " + e.getMessage());
                            System.out.println("An error occurred while searching media. Please try again.");
                        }
                        break;
                    case 4:
                        searching = false;
                        logger.info("Returning to main menu");
                        break;
                    default:
                        logger.warning("Invalid option selected in search menu");
                        System.out.println("Invalid option. Try again.");
                }
                
                logger.info(String.format("Search operation completed in %d ms", 
                    System.currentTimeMillis() - startTime));
            } catch (SQLException e) {
                logger.severe("Database connection error in search menu: " + e.getMessage());
                System.out.println("A database error occurred. Please try again.");
            }
        }
    }

    private static void exploreMenu(Scanner scanner) {
        boolean exploring = true;
        
        while (exploring) {
            System.out.println("\n=== Explore Library ===");
            System.out.println("1. Browse Books");
            System.out.println("2. Browse Magazines");
            System.out.println("3. Browse Media");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option (1-4): ");
            
            int choice = -1;
            while (choice < 1 || choice > 4) {
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                    if (choice < 1 || choice > 4) {
                        System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number between 1 and 4.");
                }
            }
            
            try (Connection connection = DatabaseConnection.getConnection()) {
                long startTime = System.currentTimeMillis();
                
                switch (choice) {
                    case 1:
                        logger.info("Browsing books...");
                        List<LibraryItem> books = BookService.getAllItems(connection);
                        displayItems("Books", books);
                        break;
                    case 2:
                        logger.info("Browsing magazines...");
                        List<Magazine> magazines = MagazineService.getAllItems(connection);
                        displayItems("Magazines", new ArrayList<>(magazines));
                        break;
                    case 3:
                        logger.info("Browsing media...");
                        List<MediaItem> media = MediaService.getAllItems(connection);
                        displayItems("Media", new ArrayList<>(media));
                        break;
                    case 4:
                        exploring = false;
                        logger.info("Returning to main menu");
                        break;
                    default:
                        logger.warning("Invalid option selected in explore menu");
                        System.out.println("Invalid option. Try again.");
                }
                
                logger.info(String.format("Explore operation completed in %d ms", 
                    System.currentTimeMillis() - startTime));
            } catch (SQLException e) {
                logger.severe("Database connection error in explore menu: " + e.getMessage());
                System.out.println("A database error occurred. Please try again.");
            }
        }
    }
}
