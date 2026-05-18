package com.bookstore.onlinebookstoremanagement.reviews;

import com.bookstore.onlinebookstoremanagement.models.Review;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewFileManager {

    private static final String FILE_PATH = "data/reviews.txt";

    public void init() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating reviews file: "
                    + e.getMessage());
        }
    }

    public List<Review> loadAllReviews() {
        List<Review> reviews = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty())
                    reviews.add(Review.fromFileString(line));
            }
        } catch (IOException e) {
            System.out.println("Error reading reviews: "
                    + e.getMessage());
        }
        return reviews;
    }

    public void saveAllReviews(List<Review> reviews) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH))) {
            for (Review r : reviews) {
                bw.write(r.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving reviews: "
                    + e.getMessage());
        }
    }

    public void appendReview(Review review) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            bw.write(review.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error appending review: "
                    + e.getMessage());
        }
    }

    // Get reviews for a specific book
    public List<Review> loadReviewsByBook(String bookId) {
        List<Review> result = new ArrayList<>();
        for (Review r : loadAllReviews()) {
            if (r.getBookId().equals(bookId))
                result.add(r);
        }
        return result;
    }

    // Get reviews by a specific user
    public List<Review> loadReviewsByUser(String userId) {
        List<Review> result = new ArrayList<>();
        for (Review r : loadAllReviews()) {
            if (r.getUserId().equals(userId))
                result.add(r);
        }
        return result;
    }

    // Check if user already reviewed this book
    public boolean hasUserReviewed(String userId,
                                   String bookId) {
        for (Review r : loadAllReviews()) {
            if (r.getUserId().equals(userId)
                    && r.getBookId().equals(bookId))
                return true;
        }
        return false;
    }
}
