package com.bookstore.onlinebookstoremanagement.cart;

import com.bookstore.onlinebookstoremanagement.catalog.BookService;
import com.bookstore.onlinebookstoremanagement.models.Book;
import com.bookstore.onlinebookstoremanagement.models.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartFileManager cartFileManager;

    @Autowired
    private BookService bookService;

    public void init() {
        cartFileManager.init();
    }

    // ─── CART METHODS ────────────────────────────────────────

    // Add book to cart
    public String addToCart(String userId,
                            String bookId,
                            int quantity) {
        Book book = bookService.getBookById(bookId);
        if (book == null)
            return "ERROR: Book not found.";
        if (quantity <= 0)
            return "ERROR: Quantity must be at least 1.";
        if (book.getStock() < quantity)
            return "ERROR: Only " + book.getStock()
                    + " copies available.";
        if (cartFileManager.itemExists(userId, bookId, false))
            return "ERROR: Book already in cart. " +
                    "Update quantity instead.";

        String id = UUID.randomUUID()
                .toString().substring(0, 8);
        CartItem item = new CartItem(id, userId, bookId,
                book.getTitle(), book.getPrice(),
                quantity, false);
        cartFileManager.appendItem(item);
        return "SUCCESS: \"" + book.getTitle()
                + "\" added to cart!";
    }

    // Remove from cart
    public String removeFromCart(String userId,
                                 String cartItemId) {
        List<CartItem> all = cartFileManager.loadAllItems();
        boolean removed = all.removeIf(i ->
                i.getCartItemId().equals(cartItemId)
                        && i.getUserId().equals(userId)
                        && !i.isWishlist());
        if (!removed)
            return "ERROR: Item not found in your cart.";
        cartFileManager.saveAllItems(all);
        return "SUCCESS: Item removed from cart.";
    }

    // Update cart quantity
    public String updateQuantity(String userId,
                                 String cartItemId,
                                 int newQty) {
        if (newQty <= 0)
            return "ERROR: Quantity must be at least 1.";

        List<CartItem> all = cartFileManager.loadAllItems();
        for (CartItem item : all) {
            if (item.getCartItemId().equals(cartItemId)
                    && item.getUserId().equals(userId)
                    && !item.isWishlist()) {
                Book book = bookService
                        .getBookById(item.getBookId());
                if (book != null && newQty > book.getStock())
                    return "ERROR: Only "
                            + book.getStock() + " in stock.";
                item.setQuantity(newQty);
                cartFileManager.saveAllItems(all);
                return "SUCCESS: Quantity updated to "
                        + newQty + ".";
            }
        }
        return "ERROR: Item not found in your cart.";
    }

    // Clear entire cart after order
    public void clearCart(String userId) {
        List<CartItem> all = cartFileManager.loadAllItems();
        all.removeIf(i ->
                i.getUserId().equals(userId)
                        && !i.isWishlist());
        cartFileManager.saveAllItems(all);
    }

    // Get cart with summary
    public Map<String, Object> getCart(String userId) {
        List<CartItem> items =
                cartFileManager.loadCartByUser(userId);
        double total = items.stream()
                .mapToDouble(CartItem::getSubtotal).sum();
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("totalItems", items.size());
        result.put("totalAmount",
                Math.round(total * 100.0) / 100.0);
        return result;
    }

    // Get cart total (used by order module)
    public double getCartTotal(String userId) {
        return cartFileManager.loadCartByUser(userId)
                .stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    // Get cart items (used by order module)
    public List<CartItem> getCartItems(String userId) {
        return cartFileManager.loadCartByUser(userId);
    }

    // ─── WISHLIST METHODS ────────────────────────────────────

    // Add to wishlist
    public String addToWishlist(String userId,
                                String bookId) {
        Book book = bookService.getBookById(bookId);
        if (book == null)
            return "ERROR: Book not found.";
        if (cartFileManager.itemExists(
                userId, bookId, true))
            return "ERROR: Book already in wishlist.";

        String id = UUID.randomUUID()
                .toString().substring(0, 8);
        CartItem item = new CartItem(id, userId, bookId,
                book.getTitle(), book.getPrice(),
                1, true);
        cartFileManager.appendItem(item);
        return "SUCCESS: \"" + book.getTitle()
                + "\" added to wishlist!";
    }

    // Remove from wishlist
    public String removeFromWishlist(String userId,
                                     String cartItemId) {
        List<CartItem> all = cartFileManager.loadAllItems();
        boolean removed = all.removeIf(i ->
                i.getCartItemId().equals(cartItemId)
                        && i.getUserId().equals(userId)
                        && i.isWishlist());
        if (!removed)
            return "ERROR: Item not found in wishlist.";
        cartFileManager.saveAllItems(all);
        return "SUCCESS: Item removed from wishlist.";
    }

    // Move wishlist item to cart
    public String moveToCart(String userId,
                             String cartItemId) {
        List<CartItem> all = cartFileManager.loadAllItems();
        for (CartItem item : all) {
            if (item.getCartItemId().equals(cartItemId)
                    && item.getUserId().equals(userId)
                    && item.isWishlist()) {

                if (cartFileManager.itemExists(
                        userId, item.getBookId(), false))
                    return "ERROR: Book already in cart.";

                Book book = bookService
                        .getBookById(item.getBookId());
                if (book == null)
                    return "ERROR: Book no longer available.";
                if (book.getStock() < 1)
                    return "ERROR: Book is out of stock.";

                item.setWishlist(false);
                cartFileManager.saveAllItems(all);
                return "SUCCESS: \""
                        + item.getBookTitle()
                        + "\" moved to cart!";
            }
        }
        return "ERROR: Item not found in wishlist.";
    }

    // Get wishlist
    public List<CartItem> getWishlist(String userId) {
        return cartFileManager.loadWishlistByUser(userId);
    }
}
