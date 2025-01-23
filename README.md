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
4. Explore
5. Exit
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

### Magazine Search Options
When selecting "Search Magazine", users are presented with:
```
Search by:
1. Title
2. Director
3. ISSN
4. General Search
```

### Media Search Options
When selecting "Search Media", users are presented with:
```
Search by:
1. Title
2. Publisher
3. Catalog Number
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

## **Logging Configuration**
The application uses SLF4J for logging with the following configuration:
- Logs are written to both console and file (`fulkoping-library.log`)
- Log levels:
  - INFO: General application flow
  - DEBUG: Detailed debugging information
  - ERROR: Critical errors and exceptions
To change logging behavior, modify the LoggerUtil class configuration.
---
## **Demo Data and User Information for Login**
The system comes preloaded with sample data for testing purposes. Below are the details of the demo data and user credentials:

- Alice:
Salt: L8gf6gUGqizJHGmYfF0QdA==
Hash: qJrYQIZwtDrRi7RTdHYtHYrRKosCA00j6VMZQyjOM6Q=

- Bob:
Salt: n7T0is1vU6X2lqyHRcAQJg==
Hash: tfaCmJiNhdasksbXCq5RkXIi3yTNYvNwg6/HLVxtWeE=


### **Demo Users**
| **Username** | **Password** | **Name**           | **Email**               |
|--------------|--------------|--------------------|-------------------------|
| `alice`      | `password1`  | Alice Johnson      | `alice@example.com`     |
| `bob`        | `password2`  | Bob Smith          | `bob@example.com`       |
### **Demo Library Items**
#### **Books**
| **Title**                     | **Author**               | **ISBN**         | **Available** |
|-------------------------------|--------------------------|------------------|---------------|
| 1984                          | George Orwell            | 9780451524935    | Yes           |
| To Kill a Mockingbird         | Harper Lee               | 9780061120084    | Yes           |
| The Great Gatsby              | F. Scott Fitzgerald      | 9780743273565    | Yes           |
| Pride and Prejudice           | Jane Austen              | 9781503290564    | Yes           |
| The Alchemist                 | Paulo Coelho             | 9780062315007    | Yes           |
#### **Magazines**
| **Title**            | **Publisher**               | **ISSN**     | **Available** |
|----------------------|-----------------------------|--------------|---------------|
| National Geographic  | National Geographic Society | 0027-9358    | Yes           |
| Time                 | Time USA, LLC               | 0040-781X    | Yes           |
| The New Yorker       | Condé Nast                  | 0028-792X    | Yes           |
| Vogue                | Condé Nast                  | 0042-8000    | Yes           |
| Wired                | Condé Nast                  | 1059-1028    | Yes           |
#### **Media**
| **Title**                                | **Director**          | **Catalog Number** | **Available** |
|------------------------------------------|-----------------------|--------------------|---------------|
| The Dark Knight                          | Christopher Nolan     | 085391178924       | Yes           |
| Inception                                | Christopher Nolan     | 883929127826       | Yes           |
| The Lord of the Rings: The Fellowship of the Ring | Peter Jackson | 794043441328       | Yes           |
| The Matrix                               | The Wachowskis        | 085391158721       | Yes           |
| Interstellar                             | Christopher Nolan     | 883929398642       | Yes           |
### **Demo Loans**
| **User** | **Item Borrowed**       | **Loan Date** | **Return Date** |
|----------|-------------------------|---------------|-----------------|
| Alice    | 1984                    | 2025-10-01    | Not returned    |
| Bob      | To Kill a Mockingbird   | 2025-10-05    | Not returned    |
### **Demo Reservations**
| **User** | **Item Reserved**       | **Reservation Date** | **Expiry Date** |
|----------|-------------------------|----------------------|-----------------|
| Alice    | The Great Gatsby        | 2025-10-10           | 2025-11-09      |
---
## **Setup and Running Instructions**
### **1. Prerequisites**
- **Java Development Kit (JDK)**: Install JDK 17
- **Docker**: Install Docker from [https://www.docker.com/](https://www.docker.com/).
- **IntelliJ IDEA**: Install IntelliJ IDEA from [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/).
- **MySQL Connector/J**: Download the `.jar` file from [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/).
---
### **2. Set Up MySQL on Docker**
1. Pull the MySQL Docker image:
   ```bash
   docker pull mysql:8.0
   ```
2. Run a MySQL container:
   ```bash
   docker run --name fulkoping-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=fulkoping_library -p 3306:3306 -d mysql:8.0
   ```
3. Verify the container is running:
   ```bash
   docker ps
   ```
---
### **3. Set Up the Database**
1. Copy the `create_database.sql` and `demo_data.sql` files to your working directory.
2. Run the scripts to create the database and populate it with sample data:
   - **For PowerShell**:
     ```bash
     Get-Content create_database.sql | docker exec -i fulkoping-mysql mysql -uroot -proot
     Get-Content demo_data.sql | docker exec -i fulkoping-mysql mysql -uroot -proot fulkoping_library
     ```
   - **For Bash**:
     ```bash
     docker exec -i fulkoping-mysql mysql -uroot -proot < create_database.sql
     docker exec -i fulkoping-mysql mysql -uroot -proot fulkoping_library < demo_data.sql
     ```
---
### **4. Set Up IntelliJ IDEA**
1. Open the project in IntelliJ IDEA:
   - Select **File > Open** and choose the `FulkopingLibrary` folder.
2. Add the MySQL Connector/J library:
   - Go to **File > Project Structure > Libraries**.
   - Click the `+` button, select **Java**, and add the `mysql-connector-java-8.0.30.jar` file.
3. Configure the database connection in `DatabaseConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/fulkoping_library";
   private static final String USER = "root";
   private static final String PASSWORD = "root";
   ```
---
### **5. Run the Application**
- IntelliJ IDEA

- Option 1:
   Run build script:  .\setup.ps1
  
- Option 2:  
1. Open the `LibraryApp.java` file.
2. Right-click and select **Run 'LibraryApp.main()'**.
3. Use the console to interact with the application.

- VSCode IDEA

- Option 1:
   Run build script:  .\setup.ps1

- Option 2:   
1. From root folder using Powershell excute:
   ./mvnw clean package && java -jar target/FulkopingLibrary-1.0-SNAPSHOT-jar-with-dependencies.jar
---
### **6. Example Workflow**
1. **Login**:
   - Enter your username and password.
2. **Search for Items**:
   - Select the type (Book, Magazine, Media).
   - Choose the search criteria (ID, ISBN/ISSN/Catalog Number, Title, Author/Director/Publisher).
   - View the search results.
3. **Borrow an Item**:
   - Enter the ID of the item you want to borrow.
4. **Return an Item**:
   - Enter the ID of the loan you want to return.
5. **Update Profile**:
   - Update your name, email, or password.
---
## **Troubleshooting**
- **Database Connection Issues**:
   - Ensure the MySQL container is running (`docker ps`).
   - Verify the database credentials in `DatabaseConnection.java`.
- **SQL Errors**:
   - Check the `create_database.sql` and `demo_data.sql` files for syntax errors.
   - Ensure the `library_items` table is created before inserting data.
- **Password Hashing Issues**:
   - Verify the `PasswordUtils.java` class is correctly implemented.
- **Logging Issues**:
   - Check log file permissions and disk space
   - Verify LoggerUtil configuration
   
---