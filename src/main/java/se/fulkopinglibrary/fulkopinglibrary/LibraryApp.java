package se.fulkopinglibrary.fulkopinglibrary;

import se.fulkopinglibrary.fulkopinglibrary.models.User;
import se.fulkopinglibrary.fulkopinglibrary.services.UserService;
import se.fulkopinglibrary.fulkopinglibrary.services.BookService;
import se.fulkopinglibrary.fulkopinglibrary.utils.SearchUtils;
import se.fulkopinglibrary.fulkopinglibrary.models.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class LibraryApp {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println("\n=== Fulköping Library System ===");
                System.out.println("1. Login");
                System.out.println("2. Signup");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            User user = UserService.login(connection, scanner);
                            if (user != null) {
                                System.out.println("Login successful!");
                                userMenu(user, scanner);
                            } else {
                                System.out.println("Invalid username or password.");
                            }
                        }
                        break;
                    case 2:
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            boolean signupSuccess = UserService.signup(connection, scanner);
                            if (signupSuccess) {
                                System.out.println("Signup successful! Please log in.");
                            } else {
                                System.out.println("Signup failed. Please try again.");
                            }
                        }
                        break;
                    case 3:
                        running = false;
                        System.out.println("Exiting the system. Goodbye!");
                        DatabaseConnection.closePool();
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            DatabaseConnection.closePool();
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
                        System.out.println("Logged out successfully.");
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
            }
        }
    }

    private static void borrowBook(Connection connection, int userId, Scanner scanner) {
        System.out.print("Enter the ID of the book you want to borrow: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        boolean success = BookService.borrowBook(connection, userId, bookId);
        if (success) {
            System.out.println("Book borrowed successfully!");
        } else {
            System.out.println("Failed to borrow the book. It may not be available.");
        }
    }

    private static void returnBook(Connection connection, int userId, Scanner scanner) {
        System.out.print("Enter the ID of the loan you want to return: ");
        int loanId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        boolean success = BookService.returnBook(connection, loanId);
        if (success) {
            System.out.println("Book returned successfully!");
        } else {
            System.out.println("Failed to return the book. Please check the loan ID.");
        }
    }

    private static void reserveBook(Connection connection, int userId, Scanner scanner) {
        System.out.print("Enter the ID of the book you want to reserve: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        boolean success = BookService.reserveBook(connection, userId, bookId);
        if (success) {
            System.out.println("Book reserved successfully!");
        } else {
            System.out.println("Failed to reserve the book. It may already be available.");
        }
    }

    private static void viewLoanHistory(Connection connection, int userId) {
        List<Book> loans = BookService.viewLoanHistory(connection, userId);
        if (loans.isEmpty()) {
            System.out.println("No loan history found.");
        } else {
            System.out.println("\nLoan History:");
            for (Book loan : loans) {
                System.out.println("ID: " + loan.getId() + ", Title: " + loan.getTitle() +
                        ", Author: " + loan.getAuthor() + ", Type: " + loan.getType() +
                        ", ISBN: " + loan.getIsbn() + ", Available: " + (loan.isAvailable() ? "Yes" : "No"));
            }
        }
    }

    private static void viewCurrentLoans(Connection connection, int userId) {
        List<Book> loans = BookService.viewCurrentLoans(connection, userId);
        if (loans.isEmpty()) {
            System.out.println("No current loans found.");
        } else {
            System.out.println("\nCurrent Loans:");
            for (Book loan : loans) {
                System.out.println("ID: " + loan.getId() + ", Title: " + loan.getTitle() +
                        ", Author: " + loan.getAuthor() + ", Type: " + loan.getType() +
                        ", ISBN: " + loan.getIsbn() + ", Available: " + (loan.isAvailable() ? "Yes" : "No"));
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
}
