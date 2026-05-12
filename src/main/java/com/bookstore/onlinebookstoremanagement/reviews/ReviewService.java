package com.bookstore.onlinebookstoremanagement.reviews;

import com.bookstore.onlinebookstoremanagement.catalog.BookService;
import com.bookstore.onlinebookstoremanagement.models.Book;
import com.bookstore.onlinebookstoremanagement.models.Order;
import com.bookstore.onlinebookstoremanagement.models.Review;
import com.bookstore.onlinebookstoremanagement.orders.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewService {

    @Autowired
    private ReviewFileManager reviewFileManager;

    @Autowired
    private BookService bookService;

    @Autowired
    private OrderService orderService;

    public void init() {
        reviewFileManager.init();
    }

    // Add a new review
    public String addReview(String bookId, String userId,
                            String username, int rating,
                            String comment) {
        // Check book exists
        Book book = bookService.getBookById(bookId);
        if (book == null)
            return "ERROR: Book not found.";

        // Validate that user has purchased this book
        String purchaseValidation = validateBookPurchase(userId, bookId);
        if (!purchaseValidation.startsWith("SUCCESS"))
            return purchaseValidation;

        // Check duplicate review
        if (reviewFileManager.hasUserReviewed(userId, bookId))
            return "ERROR: You already reviewed this book.";

        // Validate rating
        if (rating < 1 || rating > 5)
            return "ERROR: Rating must be between 1 and 5.";

        // Validate comment
        if (comment == null || comment.trim().isEmpty())
            return "ERROR: Comment cannot be empty.";

        String reviewId = UUID.randomUUID()
                .toString().substring(0, 8);
        Review review = new Review(reviewId, bookId,
                userId, username, rating, comment);
        reviewFileManager.appendReview(review);

        // Update book average rating
        updateBookRating(bookId);

        return "SUCCESS: Review added successfully!";
    }

    // Edit a review
    public String editReview(String reviewId, String userId,
                             int newRating, String newComment) {
        List<Review> reviews = reviewFileManager.loadAllReviews();
        for (Review r : reviews) {
            if (r.getReviewId().equals(reviewId)) {
                if (!r.getUserId().equals(userId))
                    return "ERROR: You can only edit " +
                            "your own reviews.";
                if (newRating < 1 || newRating > 5)
                    return "ERROR: Rating must be 1 to 5.";
                if (newComment == null
                        || newComment.trim().isEmpty())
                    return "ERROR: Comment cannot be empty.";

                r.setRating(newRating);
                r.setComment(newComment);
                reviewFileManager.saveAllReviews(reviews);
                updateBookRating(r.getBookId());
                return "SUCCESS: Review updated!";
            }
        }
        return "ERROR: Review not found.";
    }

    // Delete a review
    public String deleteReview(String reviewId,
                               String userId,
                               boolean isAdmin) {
        List<Review> reviews = reviewFileManager.loadAllReviews();
        Review toDelete = null;
        for (Review r : reviews) {
            if (r.getReviewId().equals(reviewId)) {
                if (!isAdmin && !r.getUserId().equals(userId))
                    return "ERROR: You can only delete " +
                            "your own reviews.";
                toDelete = r;
                break;
            }
        }
        if (toDelete == null)
            return "ERROR: Review not found.";

        String bookId = toDelete.getBookId();
        reviews.remove(toDelete);
        reviewFileManager.saveAllReviews(reviews);
        updateBookRating(bookId);
        return "SUCCESS: Review deleted.";
    }

    // Get all reviews for a book
    public List<Review> getReviewsByBook(String bookId) {
        return reviewFileManager.loadReviewsByBook(bookId);
    }

    // Get all reviews by a user
    public List<Review> getReviewsByUser(String userId) {
        return reviewFileManager.loadReviewsByUser(userId);
    }

    // Get all reviews (admin)
    public List<Review> getAllReviews() {
        return reviewFileManager.loadAllReviews();
    }

    // Get full book details with reviews
    public Map<String, Object> getBookWithReviews(
            String bookId) {
        Map<String, Object> result = new HashMap<>();
        Book book = bookService.getBookById(bookId);
        if (book == null) return null;

        List<Review> reviews =
                reviewFileManager.loadReviewsByBook(bookId);

        result.put("book", book);
        result.put("reviews", reviews);
        result.put("totalReviews", reviews.size());
        result.put("averageRating", book.getRating());
        return result;
    }

    // Validate that user has purchased a specific book
    public String validateBookPurchase(String userId, String bookId) {
        // Get all user's orders
        List<Order> userOrders = orderService.getMyOrders(userId);
        
        for (Order order : userOrders) {
            // Only check delivered orders
            if ("DELIVERED".equals(order.getStatus())) {
                // Check if the book is in this order's items
                for (var item : order.getItems()) {
                    if (item.getBookId().equals(bookId)) {
                        return "SUCCESS: User has purchased this book.";
                    }
                }
            }
        }
        
        return "ERROR: You can only review books you have purchased.";
    }

    // Validate order for review submission
    public String validateOrderForReview(String orderId, String userId) {
        Order order = orderService.getOrderById(orderId, userId);
        if (order == null) {
            return "ERROR: Order not found or access denied.";
        }
        
        if (!"DELIVERED".equals(order.getStatus())) {
            return "ERROR: Reviews can only be submitted for delivered orders.";
        }
        
        return "SUCCESS: Order validated for review.";
    }

    // Add bulk reviews for a delivered order
    public Map<String, Object> addBulkReviews(String userId, String username, 
                                              String orderId, List<Map<String, Object>> reviews) {
        Map<String, Object> result = new HashMap<>();
        List<String> successfulReviews = new ArrayList<>();
        List<String> failedReviews = new ArrayList<>();
        
        // First validate the order
        String validation = validateOrderForReview(orderId, userId);
        if (!validation.startsWith("SUCCESS")) {
            result.put("success", false);
            result.put("message", validation);
            return result;
        }
        
        // Process each review
        for (Map<String, Object> reviewData : reviews) {
            String bookId = (String) reviewData.get("bookId");
            Integer rating = (Integer) reviewData.get("rating");
            String comment = (String) reviewData.get("comment");
            
            try {
                // First validate that user has purchased this book
                String purchaseValidation = validateBookPurchase(userId, bookId);
                if (!purchaseValidation.startsWith("SUCCESS")) {
                    failedReviews.add(bookId + ": " + purchaseValidation);
                    continue;
                }
                
                String addResult = addReview(bookId, userId, username, rating, comment);
                if (addResult.startsWith("SUCCESS")) {
                    successfulReviews.add(bookId);
                } else {
                    failedReviews.add(bookId + ": " + addResult);
                }
            } catch (Exception e) {
                failedReviews.add(bookId + ": " + e.getMessage());
            }
        }
        
        result.put("success", !successfulReviews.isEmpty());
        result.put("successfulReviews", successfulReviews);
        result.put("failedReviews", failedReviews);
        result.put("totalSubmitted", reviews.size());
        result.put("totalSuccessful", successfulReviews.size());
        result.put("totalFailed", failedReviews.size());
        
        if (!successfulReviews.isEmpty()) {
            result.put("message", "Successfully submitted " + successfulReviews.size() + " reviews.");
        } else {
            result.put("message", "No reviews were submitted successfully.");
        }
        
        return result;
    }

    // Recalculate and update book average rating
    private void updateBookRating(String bookId) {
        List<Review> reviews =
                reviewFileManager.loadReviewsByBook(bookId);
        double avg = 0.0;
        if (!reviews.isEmpty()) {
            int total = 0;
            for (Review r : reviews) total += r.getRating();
            avg = (double) total / reviews.size();
        }
        bookService.updateBookRating(bookId, avg);
    }
}
