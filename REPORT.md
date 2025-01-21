# **Fulköping Library System Technical Report**

## **Project Overview**
The Fulköping Library System is a Java-based console application designed to manage library operations, including item management, user accounts, borrowing/returning items, and search functionality. The system uses **MySQL** for data persistence and follows a **modular architecture** to ensure scalability and maintainability.

---

## **Technical Architecture**

### **Core Components**
1. **Database Layer**
   - **MySQL database** with JDBC connectivity.
   - Unified `library_items` table for all media types (books, magazines, and media).
   - Normalized tables for user and loan management.

2. **Service Layer**
   - **BookService**: Handles item borrowing, returning, and availability tracking.
   - **UserService**: Manages user authentication, profiles, and password security.
   - **SearchService**: Provides search functionality across all media types.

3. **Model Layer**
   - **Book, Magazine, and Media classes**: Represent specific media types.
   - **LibraryItem**: Base class for common properties across all media types.
   - **User class**: Manages user authentication and profile information.

4. **Utility Layer**
   - **PasswordUtils**: Implements secure password hashing and verification using salted hashes.
   - **DatabaseConnection**: Centralized database connectivity management.

---

## **Database Schema Details**

### **Tables**
1. **library_items**
   - `item_id` (PK): Unique identifier for each library item.
   - `title`: Title of the item.
   - `author`: Author, director, or publisher of the item.
   - `type`: Type of item (`BOOK`, `MAGAZINE`, or `MEDIA`).
   - `isbn`: ISBN for books (13 digits) or `NULL` for other types.
   - `issn`: ISSN for magazines (8 digits) or `NULL` for other types.
   - `is_available`: Boolean flag indicating availability.
   - `created_at` and `updated_at`: Timestamps for tracking changes.

2. **users**
   - `user_id` (PK): Unique identifier for each user.
   - `username`: Unique username for login.
   - `password_hash`: Securely hashed password.
   - `salt`: Random salt used for password hashing.
   - `name`: Full name of the user.
   - `email`: Unique email address of the user.
   - `failed_attempts`: Number of failed login attempts.
   - `lockout_until`: Timestamp for account lockout (if applicable).

3. **loans**
   - `loan_id` (PK): Unique identifier for each loan.
   - `user_id` (FK): Reference to the user who borrowed the item.
   - `item_id` (FK): Reference to the borrowed item.
   - `loan_date`: Date the item was borrowed.
   - `return_date`: Date the item was returned (nullable).

4. **reservations**
   - `reservation_id` (PK): Unique identifier for each reservation.
   - `user_id` (FK): Reference to the user who reserved the item.
   - `item_id` (FK): Reference to the reserved item.
   - `reservation_date`: Date the reservation was made.
   - `expiry_date`: Date the reservation expires.

---

## **Recent Schema Changes**
1. **Consolidated Media Tables**:
   - Replaced separate tables for books, magazines, and media with a unified `library_items` table.
   - Added a `type` column to distinguish between different media types (`BOOK`, `MAGAZINE`, `MEDIA`).

2. **Updated Foreign Key References**:
   - Changed `book_id` references to `item_id` across all related tables (`loans` and `reservations`).

3. **Improved Data Integrity**:
   - Added constraints for `isbn` (13 digits) and `issn` (8 digits).
   - Set `isbn` and `issn` to `NULL` for media items that do not require them.

4. **Enhanced Search Functionality**:
   - Modified search methods to work with the unified `library_items` table.
   - Added support for searching by `type`, `title`, `author`, `isbn`, and `issn`.

---

## **Key Functionality**

### **Item Management**
- **Unified Handling**: Books, magazines, and media are managed through a single `library_items` table.
- **Availability Tracking**: The `is_available` flag indicates whether an item can be borrowed.
- **Loan Durations**:
  - Books: 30 days
  - Magazines: 10 days
  - Media: 10 days
- **Reservation System**:
  - Users can reserve items that are currently on loan
  - Reservations automatically expire after 30 days if not picked up
  - Reserved items are held for the reserving user when returned
  - Email notifications are sent 3 days before reservation expiration

### **User Management**
- **Secure Authentication**: Passwords are hashed using salted hashes for enhanced security.
- **Profile Updates**: Users can update their name, email, and password.
- **Loan History**: Users can view their borrowing history, including loan and return dates.

### **Search System**
- **Unified Search**: Users can search across all media types using a single interface.
- **Multiple Criteria**: Search by ID, ISBN/ISSN, title, or author/director/publisher.
- **Type-Specific Filtering**: Results can be filtered by media type (`BOOK`, `MAGAZINE`, `MEDIA`).

---

## **Future Improvements**

### **1. Enhanced Search**
- Implement **full-text search** for more accurate and flexible search results.
- Add **advanced filtering options** (e.g., by availability, publication year, or genre).

### **2. Reporting**
- Generate **usage statistics** (e.g., most borrowed items, popular genres).
- Track **user activity** (e.g., frequent borrowers, overdue items).

### **3. Security**
- Enforce **password strength requirements** (e.g., minimum length, special characters).
- Implement **account lockout** after a specified number of failed login attempts.

### **4. Scalability**
- Use **database connection pooling** to improve performance under high load.
- Implement **caching** for frequently accessed data (e.g., popular items, user profiles).

### **5. User Interface**
- Develop a **graphical interface** for a more user-friendly experience.
- Create a **mobile-friendly web interface** for remote access.

---

## **Conclusion**
The Fulköping Library System provides a robust and scalable solution for managing library operations. The recent schema changes, including the consolidation of media tables and improved search functionality, have enhanced the system's efficiency and usability. Future improvements, such as enhanced search capabilities and a graphical interface, will further elevate the user experience and system performance.
