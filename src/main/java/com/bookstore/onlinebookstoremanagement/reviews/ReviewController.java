package com.bookstore.onlinebookstoremanagement.reviews;

import com.bookstore.onlinebookstoremanagement.models.Review;
import com.bookstore.onlinebookstoremanagement.orders.OrderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() {
        reviewService.init();
    }

    // ── GET /api/reviews/book/{bookId} ───────────────────────
    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getBookWithReviews(
            @PathVariable String bookId) {
        Map<String, Object> result =
                reviewService.getBookWithReviews(bookId);
        if (result != null)
            return ResponseEntity.ok(result);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Book not found."));
    }

    // ── GET /api/reviews/user/{userId} ───────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                reviewService.getReviewsByUser(userId));
    }

    // ── GET /api/reviews/all (admin) ─────────────────────────
    @GetMapping("/all")
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(
                reviewService.getAllReviews());
    }

    // ── POST /api/reviews ────────────────────────────────────
    @PostMapping
    public ResponseEntity<Map<String, String>> addReview(
            @RequestBody Map<String, String> body) {
        int rating;
        try {
            rating = Integer.parseInt(body.get("rating"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid rating format."));
        }

        String result = reviewService.addReview(
                body.get("bookId"),
                body.get("userId"),
                body.get("username"),
                rating,
                body.get("comment"));

        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ── POST /api/reviews/validate (check if user can review book) ──
    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validateBookPurchase(
            @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String bookId = body.get("bookId");

        String validationResult = reviewService.validateBookPurchase(userId, bookId);
        
        if (validationResult.startsWith("SUCCESS")) {
            return ResponseEntity.ok(Map.of("message", validationResult));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", validationResult));
        }
    }

    // ── POST /api/reviews/bulk (for delivered orders) ───────
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> addBulkReviews(
            @RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String username = (String) body.get("username");
        String orderId = (String) body.get("orderId");
        List<Map<String, Object>> reviews = 
                (List<Map<String, Object>>) body.get("reviews");

        // Validate order is delivered
        String validationResult = reviewService.validateOrderForReview(orderId, userId);
        if (!validationResult.startsWith("SUCCESS")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", validationResult));
        }

        // Process bulk reviews
        Map<String, Object> result = reviewService.addBulkReviews(
                userId, username, orderId, reviews);

        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", result.get("message")));
        }
    }

    // ── PUT /api/reviews/{reviewId} ──────────────────────────
    @PutMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> editReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        int newRating;
        try {
            newRating = Integer.parseInt(body.get("rating"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid rating format."));
        }

        String result = reviewService.editReview(
                reviewId,
                body.get("userId"),
                newRating,
                body.get("comment"));

        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ── DELETE /api/reviews/{reviewId} ───────────────────────
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        boolean isAdmin = "true".equalsIgnoreCase(
                body.get("isAdmin"));

        String result = reviewService.deleteReview(
                reviewId,
                body.get("userId"),
                isAdmin);

        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }
}
