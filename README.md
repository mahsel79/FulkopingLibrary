# **Fulköping Library System**

The Fulköping Library System is a **Java console application** designed to help the staff and residents of Fulköping manage and explore the library's collection of books, magazines, and media. The system allows users to search for items, borrow and return books, view loan history, and update their profiles.

---

## **Technologies Used**
- **Programming Language**: Java 17
- **Database**: MySQL
- **Database Management**: Docker (for running MySQL)
- **Build Tool**: Maven
- **IDE**: IntelliJ IDEA (recommended)
- **Libraries**:
   - MySQL Connector/J (for database connectivity)
   - Java Standard Library (for console input/output and hashing)
   - SLF4J (for logging)

---

## **Folder and File Structure**
```
FulkopingLibrary/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── se/
│   │   │   │   ├── fulkopinglibrary/
│   │   │   │   │   ├── fulkopinglibrary/
│   │   │   │   │   │   ├── DatabaseConnection.java
│   │   │   │   │   │   ├── LibraryApp.java
│   │   │   │   │   │   ├── models/
│   │   │   │   │   │   │   ├── Book.java
│   │   │   │   │   │   │   ├── Magazine.java
│   │   │   │   │   │   │   ├── Media.java
│   │   │   │   │   │   │   ├── MediaItem.java
│   │   │   │   │   │   │   ├── MediaType.java
│   │   │   │   │   │   │   ├── MediaTypeImpl.java
│   │   │   │   │   │   │   ├── ItemType.java
│   │   │   │   │   │   │   ├── LibraryItem.java
│   │   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   ├── services/
│   │   │   │   │   │   │   ├── BookService.java
│   │   │   │   │   │   │   ├── MediaService.java
│   │   │   │   │   │   │   ├── MagazineService.java
│   │   │   │   │   │   │   ├── SearchBook.java
│   │   │   │   │   │   │   ├── UserService.java
│   │   │   │   │   │   ├── utils/
│   │   │   │   │   │   │   ├── LoggerUtil.java
│   │   │   │   │   │   │   ├── PasswordUtils.java
│   │   │   │   │   │   │   ├── SearchUtils.java
│   │   │   │   │   │   │   ├── SearchMagazine.java
│   │   │   │   │   │   │   ├── SearchMedia.java
├── sql/
│   ├── create_database.sql
│   ├── demo_data.sql
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .env
├── .gitignore
├── LICENSE
├── README.md
├── REPORT.md
```

---

## **Menu Details**

The application features a hierarchical menu system with clear navigation:

### Main Menu
```
=== Welcome to Fulköping Library ===
1. Login
2. Signup
3. Search
4. Exit
```

### Search Menu
```
=== Search Menu ===
1. Search Books
2. Search Magazines
3. Search Media
4. Back to Main Menu
```

### Book Search Options
When selecting "Search Books", users are presented with:
```
Search by:
1. Title
2. Author
3. ISBN
4. General Search
```

Each search type provides clear instructions and feedback during the search process.

---

## **Search and Explore Features**

The search functionality has been enhanced to provide a more intuitive experience:

1. **Search Options**:
   - Users are now presented with clear search options before entering their search term
   - Search types are categorized by item type (Books, Magazines, Media)
   - Each search type has specific parameters for better results

2. **Search Process**:
   - Users select their search type first
   - The system then prompts for the search term
   - Results are displayed with clear formatting and relevant details

3. **Search Results**:
   - Results are grouped by type
   - Each result shows availability status
   - Users can easily navigate back to search or main menu

---

## **Features**
1. **Search for Items**:
   - Search for books, magazines, and media by ID, ISBN/ISSN/Catalog Number, Title, or Author/Director/Publisher.
   - Results are grouped by type (Books, Magazines, Media) and displayed in a clean, organized format.

2. **Borrow and Return Items**:
   - Users can borrow and return library items using the unified `library_items` table.
   - An item can only be borrowed by one user at a time.

3. **View Loan History**:
   - Users can view their loan history, including the loan and return dates.

4. **User Login and Profile Management**:
   - Users can log in with a username and password.
   - Passwords are securely hashed and salted before being stored in the database.
   - Users can update their profile information (name, email, password).

5. **Reserve Items**:
   - Users can reserve items that are currently on loan.
   - Reservations expire after 30 days if not picked up.
   - Books may be borrowed for 30 days, magazines and other media for 10 days.

---

[Rest of the existing README content remains unchanged...]
