package com.bookstore.onlinebookstoremanagement.models;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private String category;
    private double price;
    private int stock;
    private String description;
    private String imageUrl;
    private double rating;

    public Book() {}

    public Book(String bookId, String title, String author,
                String category, double price,
                int stock, String description) {
        this.bookId      = bookId;
        this.title       = title;
        this.author      = author;
        this.category    = category;
        this.price       = price;
        this.stock       = stock;
        this.description = description;
        this.imageUrl    = "";
        this.rating      = 0.0;
    }

    // Getters
    public String getBookId()      { return bookId; }
    public String getTitle()       { return title; }
    public String getAuthor()      { return author; }
    public String getCategory()    { return category; }
    public double getPrice()       { return price; }
    public int    getStock()       { return stock; }
    public String getDescription() { return description; }
    public String getImageUrl()    { return imageUrl; }
    public double getRating()      { return rating; }

    // Setters
    public void setBookId(String bookId)         { this.bookId      = bookId; }
    public void setTitle(String title)           { this.title       = title; }
    public void setAuthor(String author)         { this.author      = author; }
    public void setCategory(String category)     { this.category    = category; }
    public void setPrice(double price)           { this.price       = price; }
    public void setStock(int stock)              { this.stock       = stock; }
    public void setDescription(String desc)      { this.description = desc; }
    public void setImageUrl(String imageUrl)     { this.imageUrl    = imageUrl; }
    public void setRating(double rating)         { this.rating      = rating; }

    public String toFileString() {
        return bookId + "|" + title + "|" + author + "|" +
                category + "|" + price + "|" + stock + "|" +
                description + "|" + imageUrl + "|" + rating;
    }

    public static Book fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        Book b = new Book(p[0], p[1], p[2], p[3],
                Double.parseDouble(p[4]),
                Integer.parseInt(p[5]), p[6]);
        b.setImageUrl(p.length > 7 ? p[7] : "");
        b.setRating(Double.parseDouble(p.length > 8 ? p[8] : "0"));
        return b;
    }
}