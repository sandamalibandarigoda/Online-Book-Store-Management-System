package com.bookstore.onlinebookstoremanagement.admin;

import com.bookstore.onlinebookstoremanagement.auth.AuthService;
import com.bookstore.onlinebookstoremanagement.catalog.BookService;
import com.bookstore.onlinebookstoremanagement.models.Book;
import com.bookstore.onlinebookstoremanagement.models.Order;
import com.bookstore.onlinebookstoremanagement.models.Review;
import com.bookstore.onlinebookstoremanagement.models.User;
import com.bookstore.onlinebookstoremanagement.orders.OrderService;
import com.bookstore.onlinebookstoremanagement.reviews.ReviewService;
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

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    // ─── DASHBOARD ───────────────────────────────────────────

    public Map<String, Object> getDashboard() {
        List<User>   users   = authService.getAllUsers();
        List<Book>   books   = bookService.getAllBooks();
        List<Order>  orders  = orderService.getAllOrders();
        List<Review> reviews = reviewService.getAllReviews();

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

        // Order stats
        long totalOrders   = orders.size();
        long pendingOrders = orders.stream()
                .filter(o -> o.getStatus()
                        .equals("PENDING")
                        || o.getStatus()
                        .equals("CONFIRMED"))
                .count();
        double revenue     = orders.stream()
                .filter(o -> o.getStatus()
                        .equals("DELIVERED"))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Review stats
        long totalReviews = reviews.size();

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

        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("total", totalOrders);
        orderStats.put("pending", pendingOrders);
        orderStats.put("revenue",
                Math.round(revenue * 100.0) / 100.0);

        Map<String, Object> reviewStats = new HashMap<>();
        reviewStats.put("total", totalReviews);

        dashboard.put("users", userStats);
        dashboard.put("books", bookStats);
        dashboard.put("orders", orderStats);
        dashboard.put("reviews", reviewStats);
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

    // ─── ORDER MANAGEMENT ────────────────────────────────────

    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderService.getAllOrders().stream()
                .filter(o -> o.getStatus()
                        .equalsIgnoreCase(status))
                .collect(java.util.stream
                        .Collectors.toList());
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderService.getAllOrders().stream()
                .filter(o -> o.getUserId().equals(userId))
                .collect(java.util.stream
                        .Collectors.toList());
    }

    public String updateOrderStatus(String orderId,
                                    String status) {
        return orderService.updateOrderStatus(
                orderId, status);
    }

    public Map<String, Object> getOrderStats() {
        return orderService.getOrderStats();
    }

    // ─── REVIEW MANAGEMENT ───────────────────────────────────

    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    public List<Review> getReviewsByBook(String bookId) {
        return reviewService.getReviewsByBook(bookId);
    }

    public String deleteReview(String reviewId) {
        return reviewService.deleteReview(
                reviewId, "", true);
    }

    public String deleteUser(String userId,
                             String adminId) {
        if (userId.equals(adminId))
            return "ERROR: Cannot delete your own account.";
        return authService.deleteUser(userId);
    }
}
