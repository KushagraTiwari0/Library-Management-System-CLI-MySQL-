import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Library {
    static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    static final String USER = "root";
    static final String PASS = "";

    static Connection conn;
    static Scanner sc = new Scanner(System.in);
    static int loggedInUserId;
    static String loggedInRole;

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            System.out.println("Welcome to Library System");
            System.out.print("Do you want to (1) Register or (2) Login? Enter 1 or 2: ");
            int choice = sc.nextInt(); sc.nextLine();

            if (choice == 1) register();
            else login();

            if ("admin".equals(loggedInRole)) {
                adminMenu();
            } else {
                userMenu();
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void register() throws SQLException {
        System.out.print("Choose a username: ");
        String username = sc.nextLine();
        System.out.print("Choose a password: ");
        String password = sc.nextLine();

        String checkSql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, username);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
            System.out.println("Username already taken. Try logging in.");
            System.exit(0);
        }

        String sql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, 'user', 'pending')";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.executeUpdate();

        System.out.println("Registered successfully. Waiting for admin approval.");
        System.exit(0);
    }

    static void login() throws SQLException {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            if (!"approved".equals(rs.getString("status"))) {
                System.out.println("Your account is not approved yet.");
                System.exit(0);
            }
            loggedInUserId = rs.getInt("id");
            loggedInRole = rs.getString("role");
            System.out.println("Login successful as " + loggedInRole);
        } else {
            System.out.println("Invalid login. Exiting.");
            System.exit(0);
        }
    }

    static void adminMenu() throws SQLException {
        while (true) {
            System.out.println("\n1. Add Book\n2. View All Book Issues\n3. View All Users\n4. Approve Users\n5. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1 -> addBook();
                case 2 -> viewAllIssuedBooks();
                case 3 -> viewAllUsers();
                case 4 -> approveUsers();
                case 5 -> System.exit(0);
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void userMenu() throws SQLException {
        while (true) {
            System.out.println("\n1. View Available Books\n2. Issue Book\n3. View My Issued Books");
            boolean hasIssuedBook = hasIssuedBooks();
            if (hasIssuedBook) System.out.println("4. Return Book\n5. Exit");
            else System.out.println("4. Exit");

            System.out.print("Choose: ");
            int choice = sc.nextInt(); sc.nextLine();

            if (!hasIssuedBook) {
                switch (choice) {
                    case 1 -> viewAvailableBooks();
                    case 2 -> issueBook();
                    case 3 -> viewMyBooks();
                    case 4 -> System.exit(0);
                    default -> System.out.println("Invalid choice.");
                }
            } else {
                switch (choice) {
                    case 1 -> viewAvailableBooks();
                    case 2 -> issueBook();
                    case 3 -> viewMyBooks();
                    case 4 -> returnBook();
                    case 5 -> System.exit(0);
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    static void addBook() throws SQLException {
        System.out.print("Title: ");
        String title = sc.nextLine();
        System.out.print("Author: ");
        String author = sc.nextLine();

        String sql = "INSERT INTO books (title, author, isIssued) VALUES (?, ?, false)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, title);
        stmt.setString(2, author);
        stmt.executeUpdate();

        System.out.println("Book added.");
    }

    static void viewAvailableBooks() throws SQLException {
        String sql = "SELECT * FROM books WHERE isIssued = false";
        ResultSet rs = conn.createStatement().executeQuery(sql);

        System.out.println("\nAvailable Books:");
        while (rs.next()) {
            System.out.printf("ID: %d | Title: %s | Author: %s\n", rs.getInt("id"), rs.getString("title"), rs.getString("author"));
        }
    }

    static void issueBook() throws SQLException {
        viewAvailableBooks();
        System.out.print("Enter Book ID to issue: ");
        int bookId = sc.nextInt(); sc.nextLine();

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(7);

        String transSql = "INSERT INTO transactions (user_id, book_id, issue_date, due_date, returned) VALUES (?, ?, ?, ?, false)";
        PreparedStatement transStmt = conn.prepareStatement(transSql);
        transStmt.setInt(1, loggedInUserId);
        transStmt.setInt(2, bookId);
        transStmt.setDate(3, Date.valueOf(issueDate));
        transStmt.setDate(4, Date.valueOf(dueDate));
        transStmt.executeUpdate();

        String bookSql = "UPDATE books SET isIssued = true WHERE id = ?";
        PreparedStatement bookStmt = conn.prepareStatement(bookSql);
        bookStmt.setInt(1, bookId);
        bookStmt.executeUpdate();

        System.out.println("Book issued. Return by: " + dueDate);
    }

    static void viewMyBooks() throws SQLException {
        String sql = "SELECT b.title, b.author, t.issue_date, t.due_date FROM books b JOIN transactions t ON b.id = t.book_id WHERE t.user_id = ? AND t.returned = false";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, loggedInUserId);
        ResultSet rs = stmt.executeQuery();

        System.out.println("\nYour Issued Books:");
        while (rs.next()) {
            LocalDate due = rs.getDate("due_date").toLocalDate();
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due);
            System.out.printf("Title: %s | Author: %s | Due in: %d days\n", rs.getString("title"), rs.getString("author"), daysLeft);
        }
    }

    static void returnBook() throws SQLException {
        String sql = "SELECT b.id, b.title FROM books b JOIN transactions t ON b.id = t.book_id WHERE t.user_id = ? AND t.returned = false";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, loggedInUserId);
        ResultSet rs = stmt.executeQuery();

        System.out.println("\nBooks you can return:");
        List<Integer> validIds = new ArrayList<>();
        while (rs.next()) {
            int bookId = rs.getInt("id");
            validIds.add(bookId);
            System.out.printf("ID: %d | Title: %s\n", bookId, rs.getString("title"));
        }

        System.out.print("Enter Book ID to return: ");
        int bookId = sc.nextInt(); sc.nextLine();

        if (!validIds.contains(bookId)) {
            System.out.println("Invalid Book ID.");
            return;
        }

        String updateTransaction = "UPDATE transactions SET returned = true WHERE user_id = ? AND book_id = ? AND returned = false";
        PreparedStatement upStmt = conn.prepareStatement(updateTransaction);
        upStmt.setInt(1, loggedInUserId);
        upStmt.setInt(2, bookId);
        upStmt.executeUpdate();

        String updateBook = "UPDATE books SET isIssued = false WHERE id = ?";
        PreparedStatement bookStmt = conn.prepareStatement(updateBook);
        bookStmt.setInt(1, bookId);
        bookStmt.executeUpdate();

        System.out.println("Book returned successfully.");
    }

    static void viewAllIssuedBooks() throws SQLException {
        String sql = "SELECT u.username, b.title, t.issue_date, t.due_date FROM transactions t JOIN users u ON t.user_id = u.id JOIN books b ON t.book_id = b.id WHERE t.returned = false";
        ResultSet rs = conn.createStatement().executeQuery(sql);

        System.out.println("\nIssued Books:");
        while (rs.next()) {
            LocalDate due = rs.getDate("due_date").toLocalDate();
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due);
            System.out.printf("User: %s | Book: %s | Issued: %s | Due in: %d days\n",
                    rs.getString("username"), rs.getString("title"), rs.getDate("issue_date"), daysLeft);
        }
    }

    static void viewAllUsers() throws SQLException {
        String sql = "SELECT id, username, role, status FROM users";
        ResultSet rs = conn.createStatement().executeQuery(sql);

        System.out.println("\nUsers:");
        while (rs.next()) {
            System.out.printf("ID: %d | Username: %s | Role: %s | Status: %s\n",
                    rs.getInt("id"), rs.getString("username"), rs.getString("role"), rs.getString("status"));
        }
    }

    static void approveUsers() throws SQLException {
        String sql = "SELECT id, username FROM users WHERE status = 'pending'";
        ResultSet rs = conn.createStatement().executeQuery(sql);

        System.out.println("\nPending Users:");
        while (rs.next()) {
            System.out.printf("ID: %d | Username: %s\n", rs.getInt("id"), rs.getString("username"));
        }

        System.out.print("Enter user ID to approve or 0 to skip: ");
        int id = sc.nextInt(); sc.nextLine();
        if (id != 0) {
            String approveSql = "UPDATE users SET status = 'approved' WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(approveSql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("User approved.");
        }
    }

    static boolean hasIssuedBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ? AND returned = false";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, loggedInUserId);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }
}
