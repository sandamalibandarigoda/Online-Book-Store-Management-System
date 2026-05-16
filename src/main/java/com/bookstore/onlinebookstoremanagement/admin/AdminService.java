package com.bookstore.onlinebookstoremanagement.admin;

import com.bookstore.onlinebookstoremanagement.auth.AuthService;
import com.bookstore.onlinebookstoremanagement.catalog.BookService;
import com.bookstore.onlinebookstoremanagement.models.Book;
import com.bookstore.onlinebookstoremanagement.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private AuthService authService;

    @Autowired
    private BookService bookService;

    // ─── DASHBOARD ───────────────────────────────────────────

    public Map<String, Object> getDashboard() {
        List<User>   users   = authService.getAllUsers();
        List<Book>   books   = bookService.getAllBooks();

        // User stats
        long totalUsers  = users.size();
        long admins      = users.stream()
                .filter(u -> u.getRole()
                        .equalsIgnoreCase("admin"))
                .count();
        long customers   = users.stream()
                .filter(u -> u.getRole()
                        .equalsIgnoreCase("customer"))
                .count();

        // Book stats
        long totalBooks  = books.size();
        long outOfStock  = books.stream()
                .filter(b -> b.getStock() == 0)
                .count();
        long lowStock    = books.stream()
                .filter(b -> b.getStock() > 0
                        && b.getStock() < 5)
                .count();

        // Build dashboard map
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", totalUsers);
        userStats.put("admins", admins);
        userStats.put("customers", customers);

        Map<String, Object> bookStats = new HashMap<>();
        bookStats.put("total", totalBooks);
        bookStats.put("outOfStock", outOfStock);
        bookStats.put("lowStock", lowStock);

        dashboard.put("users", userStats);
        dashboard.put("books", bookStats);
        return dashboard;
    }

    // ─── USER MANAGEMENT ─────────────────────────────────────

    public List<User> getAllUsers() {
        List<User> users = authService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    public List<User> searchUsers(String keyword) {
        List<User> result = new ArrayList<>();
        for (User u : authService.getAllUsers()) {
            if (u.getUsername().toLowerCase()
                    .contains(keyword.toLowerCase())
                    || u.getEmail().toLowerCase()
                    .contains(keyword.toLowerCase())) {
                u.setPassword(null);
                result.add(u);
            }
        }
        return result;
    }

    public String promoteToAdmin(String userId) {
        List<User> users = authService.getAllUsers();
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                if (u.getRole().equalsIgnoreCase("admin"))
                    return "INFO: User is already an admin.";
                User updated = new User(
                        u.getUserId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getPassword(),
                        "admin");
                updated.setFullName(u.getFullName());
                updated.setAddress(u.getAddress());
                updated.setPhone(u.getPhone());
                users.set(users.indexOf(u), updated);
                authService.saveAllUsers(users);
                return "SUCCESS: " + u.getUsername()
                        + " promoted to Admin.";
            }
        }
        return "ERROR: User not found.";
    }

    public Map<String, Object> getUserStats() {
        List<User> users = authService.getAllUsers();
        long admins = users.stream()
                .filter(u -> u.getRole()
                        .equalsIgnoreCase("admin"))
                .count();
        long customers = users.stream()
                .filter(u -> u.getRole()
                        .equalsIgnoreCase("customer"))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("admins", admins);
        stats.put("customers", customers);
        return stats;
    }

    // ─── BOOK MANAGEMENT ─────────────────────────────────────

    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    public List<Book> getLowStockBooks(int threshold) {
        List<Book> result = new ArrayList<>();
        for (Book b : bookService.getAllBooks()) {
            if (b.getStock() < threshold)
                result.add(b);
        }
        return result;
    }

    public String addBook(Book book) {
        return bookService.addBook(book);
    }

    public String editBook(String bookId, Book book) {
        return bookService.editBook(bookId, book);
    }

    public String deleteBook(String bookId) {
        return bookService.deleteBook(bookId);
    }

    public String restockBook(String bookId, int qty) {
        return bookService.restockBook(bookId, qty);
    }

    public String deleteUser(String userId,
                             String adminId) {
        if (userId.equals(adminId))
            return "ERROR: Cannot delete your own account.";
        return authService.deleteUser(userId);
    }
}
