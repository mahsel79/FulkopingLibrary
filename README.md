# **Fulköping Library System**

The Fulköping Library System is a **Java console application** designed to help the staff and residents of Fulköping manage and explore the library's collection of books, magazines, and media. The system allows users to search for items, borrow and return books, view loan history, and update their profiles.

---

## **Technologies Used**
- **Programming Language**: Java
- **Database**: MySQL
- **Database Management**: Docker (for running MySQL)
- **Build Tool**: Maven (optional)
- **IDE**: IntelliJ IDEA (recommended)
- **Libraries**:
   - MySQL Connector/J (for database connectivity)
   - Java Standard Library (for console input/output and hashing)

---

## **Folder and File Structure**
```
FulkopingLibrary/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── fulkoping/
│   │   │   │   ├── DatabaseConnection.java
│   │   │   │   ├── LibraryApp.java
│   │   │   │   ├── models/
│   │   │   │   │   ├── Book.java
│   │   │   │   │   ├── Magazine.java
│   │   │   │   │   ├── Media.java
│   │   │   │   ├── services/
│   │   │   │   │   ├── BookService.java
│   │   │   │   ├── utils/
│   │   │   │   │   ├── PasswordUtils.java
├── sql/
│   ├── create_database.sql
│   ├── demo_data.sql
├── lib/
│   ├── mysql-connector-java-8.0.30.jar
├── README.md
```

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

---

## **Demo Data and User Information for Login**

The system comes preloaded with sample data for testing purposes. Below are the details of the demo data and user credentials:

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
| Alice    | 1984                    | 2023-10-01    | Not returned    |
| Bob      | To Kill a Mockingbird   | 2023-10-05    | Not returned    |

### **Demo Reservations**
| **User** | **Item Reserved**       | **Reservation Date** | **Expiry Date** |
|----------|-------------------------|----------------------|-----------------|
| Alice    | The Great Gatsby        | 2023-10-10           | 2023-11-09      |

---

## **Setup and Running Instructions**

### **1. Prerequisites**
- **Java Development Kit (JDK)**: Install JDK 11 or later.
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
1. Open the `LibraryApp.java` file.
2. Right-click and select **Run 'LibraryApp.main()'**.
3. Use the console to interact with the application.

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

---

## **License**
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
