package com.bookstore.onlinebookstoremanagement.models;

import java.time.LocalDate;

public class Review {
    private String reviewId;
    private String bookId;
    private String userId;
    private String username;
    private int rating;
    private String comment;
    private String date;

    public Review() {}

    public Review(String reviewId, String bookId, String userId,
                  String username, int rating, String comment) {
        this.reviewId = reviewId;
        this.bookId   = bookId;
        this.userId   = userId;
        this.username = username;
        this.rating   = rating;
        this.comment  = comment;
        this.date     = LocalDate.now().toString();
    }

    // Getters
    public String getReviewId() { return reviewId; }
    public String getBookId()   { return bookId; }
    public String getUserId()   { return userId; }
    public String getUsername() { return username; }
    public int    getRating()   { return rating; }
    public String getComment()  { return comment; }
    public String getDate()     { return date; }

    // Setters
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public void setBookId(String bookId)     { this.bookId   = bookId; }
    public void setUserId(String userId)     { this.userId   = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setRating(int rating)        { this.rating   = rating; }
    public void setComment(String comment)   { this.comment  = comment; }
    public void setDate(String date)         { this.date     = date; }

    public String toFileString() {
        return reviewId + "|" + bookId + "|" + userId + "|" +
                username + "|" + rating + "|" + comment + "|" + date;
    }

    public static Review fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        Review r = new Review(p[0], p[1], p[2], p[3],
                Integer.parseInt(p[4]), p[5]);
        r.setDate(p[6]);
        return r;
    }
}
