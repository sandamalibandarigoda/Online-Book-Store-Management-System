package com.bookstore.onlinebookstoremanagement.catalog;

import com.bookstore.onlinebookstoremanagement.models.Book;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    @PostConstruct
    public void init() {
        bookService.init();
    }

    // ── GET /api/books ───────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // ── GET /api/books/{bookId} ──────────────────────────────
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBookById(
            @PathVariable String bookId) {
        Book book = bookService.getBookById(bookId);
        if (book != null)
            return ResponseEntity.ok(book);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Book not found."));
    }

    // ── GET /api/books/search/title?keyword= ────────────────
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchByTitle(
            @RequestParam String keyword) {
        return ResponseEntity.ok(
                bookService.searchByTitle(keyword));
    }

    // ── GET /api/books/search/author?keyword= ───────────────
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchByAuthor(
            @RequestParam String keyword) {
        return ResponseEntity.ok(
                bookService.searchByAuthor(keyword));
    }

    // ── GET /api/books/category?name= ───────────────────────
    @GetMapping("/category")
    public ResponseEntity<List<Book>> filterByCategory(
            @RequestParam String name) {
        return ResponseEntity.ok(
                bookService.filterByCategory(name));
    }

    // ── GET /api/books/price?max= ────────────────────────────
    @GetMapping("/price")
    public ResponseEntity<List<Book>> filterByMaxPrice(
            @RequestParam double max) {
        return ResponseEntity.ok(
                bookService.filterByMaxPrice(max));
    }

    // ── GET /api/books/categories ────────────────────────────
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(
                bookService.getAllCategories());
    }

    // ── GET /api/books/sort/price-asc ───────────────────────
    @GetMapping("/sort/price-asc")
    public ResponseEntity<List<Book>> sortByPriceLowToHigh() {
        return ResponseEntity.ok(
                bookService.sortByPriceLowToHigh());
    }

    // ── GET /api/books/sort/price-desc ──────────────────────
    @GetMapping("/sort/price-desc")
    public ResponseEntity<List<Book>> sortByPriceHighToLow() {
        return ResponseEntity.ok(
                bookService.sortByPriceHighToLow());
    }

    // ── GET /api/books/sort/rating ───────────────────────────
    @GetMapping("/sort/rating")
    public ResponseEntity<List<Book>> sortByRating() {
        return ResponseEntity.ok(bookService.sortByRating());
    }

    // ── POST /api/books (admin) ──────────────────────────────
    @PostMapping
    public ResponseEntity<Map<String, String>> addBook(
            @RequestBody Book book) {
        String result = bookService.addBook(book);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ── PUT /api/books/{bookId} (admin) ──────────────────────
    @PutMapping("/{bookId}")
    public ResponseEntity<Map<String, String>> editBook(
            @PathVariable String bookId,
            @RequestBody Book book) {
        String result = bookService.editBook(bookId, book);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ── DELETE /api/books/{bookId} (admin) ───────────────────
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Map<String, String>> deleteBook(
            @PathVariable String bookId) {
        String result = bookService.deleteBook(bookId);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ── PUT /api/books/{bookId}/restock (admin) ──────────────
    @PutMapping("/{bookId}/restock")
    public ResponseEntity<Map<String, String>> restockBook(
            @PathVariable String bookId,
            @RequestBody Map<String, Integer> body) {
        String result = bookService.restockBook(
                bookId, body.get("quantity"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }
}