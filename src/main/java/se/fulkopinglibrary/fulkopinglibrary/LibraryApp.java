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
import se.fulkopinglibrary.fulkopinglibrary.utils.SearchUtils;
import se.fulkopinglibrary.fulkopinglibrary.models.Book;
import se.fulkopinglibrary.fulkopinglibrary.models.LibraryItem;
import se.fulkopinglibrary.fulkopinglibrary.models.Magazine;
import se.fulkopinglibrary.fulkopinglibrary.models.MediaItem;

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
                System.out.println("4. Exit");
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
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            logger.info("Opening search menu...");
                            searchMenu(connection, scanner);
                        } catch (SQLException e) {
                            logger.severe("Database connection error in search menu: " + e.getMessage());
                        } finally {
                            DatabaseConnection.closePool();
                        }
                        break;
                    case 4:
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

    private static void searchMenu(Connection connection, Scanner scanner) throws SQLException {
        boolean searching = true;
        
        try {
            while (searching) {
                System.out.println("\n=== Search Menu ===");
                System.out.println("1. Search Books");
                System.out.println("2. Search Magazines");
                System.out.println("3. Search Media");
                System.out.println("4. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                long startTime = System.currentTimeMillis();
                try {
                    switch (choice) {
                        case 1:
                            logger.info("Searching books...");
                            SearchUtils.searchBooks(connection, scanner);
                            break;
                        case 2:
                            logger.info("Searching magazines...");
                            System.out.print("Enter search term: ");
                            String magazineSearchTerm = scanner.nextLine();
                            System.out.print("Search by (title/publisher/issn): ");
                            String magazineField = scanner.nextLine();
                            
                            List<Magazine> magazines = BookService.searchMagazines(connection, magazineField, magazineSearchTerm);
                            if (magazines.isEmpty()) {
                                System.out.println("No magazines found matching your search.");
                            } else {
                                System.out.println("\nFound Magazines:");
                                for (Magazine magazine : magazines) {
                                    System.out.println("ID: " + magazine.getId() + 
                                        ", Title: " + magazine.getTitle() +
                                        ", Publisher: " + magazine.getPublisher() +
                                        ", ISSN: " + magazine.getIssn() +
                                        ", Available: " + (magazine.isAvailable() ? "Yes" : "No"));
                                }
                            }
                            break;
                        case 3:
                            logger.info("Searching media...");
                            System.out.print("Enter search term: ");
                            String mediaSearchTerm = scanner.nextLine();
                            System.out.print("Search by (title/director/catalog): ");
                            String mediaField = scanner.nextLine();
                            
                            List<LibraryItem> mediaItems = BookService.searchLibraryItems(connection, mediaField, mediaSearchTerm)
                                .stream()
                                .filter(item -> item instanceof MediaItem)
                                .toList();
                                
                            if (mediaItems.isEmpty()) {
                                System.out.println("No media items found matching your search.");
                            } else {
                                System.out.println("\nFound Media Items:");
                                for (LibraryItem item : mediaItems) {
                                    MediaItem media = (MediaItem) item;
                                    System.out.println("ID: " + media.getId() + 
                                        ", Title: " + media.getTitle() +
                                        ", Director: " + media.getDirector() +
                                        ", Catalog Number: " + media.getCatalogNumber() +
                                        ", Available: " + (media.isAvailable() ? "Yes" : "No"));
                                }
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
                } catch (Exception e) {
                    logger.severe("Error during search operation: " + e.getMessage());
                    System.out.println("An error occurred during search. Please try again.");
                }
                
                logger.info(String.format("Search operation completed in %d ms", 
                    System.currentTimeMillis() - startTime));
            }
        } finally {
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }
}
