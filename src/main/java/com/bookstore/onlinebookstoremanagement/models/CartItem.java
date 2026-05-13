package com.bookstore.onlinebookstoremanagement.models;

public class CartItem {
    private String cartItemId;
    private String userId;
    private String bookId;
    private String bookTitle;
    private double price;
    private int quantity;
    private boolean wishlist;

    public CartItem() {}

    public CartItem(String cartItemId, String userId,
                    String bookId, String bookTitle,
                    double price, int quantity, boolean wishlist) {
        this.cartItemId = cartItemId;
        this.userId     = userId;
        this.bookId     = bookId;
        this.bookTitle  = bookTitle;
        this.price      = price;
        this.quantity   = quantity;
        this.wishlist   = wishlist;
    }

    // Getters
    public String  getCartItemId() { return cartItemId; }
    public String  getUserId()     { return userId; }
    public String  getBookId()     { return bookId; }
    public String  getBookTitle()  { return bookTitle; }
    public double  getPrice()      { return price; }
    public int     getQuantity()   { return quantity; }
    public boolean isWishlist()    { return wishlist; }
    public double  getSubtotal()   { return price * quantity; }

    // Setters
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }
    public void setUserId(String userId)         { this.userId     = userId; }
    public void setBookId(String bookId)         { this.bookId     = bookId; }
    public void setBookTitle(String bookTitle)   { this.bookTitle  = bookTitle; }
    public void setPrice(double price)           { this.price      = price; }
    public void setQuantity(int quantity)        { this.quantity   = quantity; }
    public void setWishlist(boolean wishlist)    { this.wishlist   = wishlist; }

    public String toFileString() {
        return cartItemId + "|" + userId + "|" + bookId + "|" +
                bookTitle + "|" + price + "|" + quantity + "|" + wishlist;
    }

    public static CartItem fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        return new CartItem(p[0], p[1], p[2], p[3],
                Double.parseDouble(p[4]),
                Integer.parseInt(p[5]),
                Boolean.parseBoolean(p[6]));
    }
}
