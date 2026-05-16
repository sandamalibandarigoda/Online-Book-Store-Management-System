package com.bookstore.onlinebookstoremanagement.admin;

import com.bookstore.onlinebookstoremanagement.models.Book;
import com.bookstore.onlinebookstoremanagement.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ─── DASHBOARD ───────────────────────────────────────────

    // GET /api/admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(
                adminService.getDashboard());
    }

    // ─── USER MANAGEMENT ─────────────────────────────────────

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(
                adminService.getAllUsers());
    }

    // GET /api/admin/users/search?keyword=
    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam String keyword) {
        return ResponseEntity.ok(
                adminService.searchUsers(keyword));
    }

    // GET /api/admin/users/stats
    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        return ResponseEntity.ok(
                adminService.getUserStats());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        String adminId = body.get("adminId");
        String result  = adminService.deleteUser(
                userId, adminId);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // PUT /api/admin/users/{userId}/promote
    @PutMapping("/users/{userId}/promote")
    public ResponseEntity<Map<String, String>> promoteUser(
            @PathVariable String userId) {
        String result = adminService.promoteToAdmin(userId);
        if (result.startsWith("SUCCESS")
                || result.startsWith("INFO"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ─── BOOK MANAGEMENT ─────────────────────────────────────

    // GET /api/admin/books
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(
                adminService.getAllBooks());
    }

    // GET /api/admin/books/low-stock?threshold=
    @GetMapping("/books/low-stock")
    public ResponseEntity<List<Book>> getLowStockBooks(
            @RequestParam(defaultValue = "5")
            int threshold) {
        return ResponseEntity.ok(
                adminService.getLowStockBooks(threshold));
    }

    // POST /api/admin/books
    @PostMapping("/books")
    public ResponseEntity<Map<String, String>> addBook(
            @RequestBody Book book) {
        String result = adminService.addBook(book);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // PUT /api/admin/books/{bookId}
    @PutMapping("/books/{bookId}")
    public ResponseEntity<Map<String, String>> editBook(
            @PathVariable String bookId,
            @RequestBody Book book) {
        String result = adminService.editBook(bookId, book);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // DELETE /api/admin/books/{bookId}
    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<Map<String, String>> deleteBook(
            @PathVariable String bookId) {
        String result = adminService.deleteBook(bookId);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // PUT /api/admin/books/{bookId}/restock
    @PutMapping("/books/{bookId}/restock")
    public ResponseEntity<Map<String, String>> restockBook(
            @PathVariable String bookId,
            @RequestBody Map<String, Integer> body) {
        String result = adminService.restockBook(
                bookId, body.get("quantity"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }
}
