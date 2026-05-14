package com.bookstore.onlinebookstoremanagement.cart;

import com.bookstore.onlinebookstoremanagement.models.CartItem;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartFileManager {

    private static final String FILE_PATH = "data/cart.txt";

    public void init() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating cart file: "
                    + e.getMessage());
        }
    }

    public List<CartItem> loadAllItems() {
        List<CartItem> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty())
                    items.add(CartItem.fromFileString(line));
            }
        } catch (IOException e) {
            System.out.println("Error reading cart: "
                    + e.getMessage());
        }
        return items;
    }

    public void saveAllItems(List<CartItem> items) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH))) {
            for (CartItem item : items) {
                bw.write(item.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving cart: "
                    + e.getMessage());
        }
    }

    public void appendItem(CartItem item) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            bw.write(item.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error appending cart item: "
                    + e.getMessage());
        }
    }

    // Load cart items only (not wishlist)
    public List<CartItem> loadCartByUser(String userId) {
        List<CartItem> result = new ArrayList<>();
        for (CartItem item : loadAllItems()) {
            if (item.getUserId().equals(userId)
                    && !item.isWishlist())
                result.add(item);
        }
        return result;
    }

    // Load wishlist items only
    public List<CartItem> loadWishlistByUser(String userId) {
        List<CartItem> result = new ArrayList<>();
        for (CartItem item : loadAllItems()) {
            if (item.getUserId().equals(userId)
                    && item.isWishlist())
                result.add(item);
        }
        return result;
    }

    // Check if book already exists in cart or wishlist
    public boolean itemExists(String userId,
                              String bookId,
                              boolean isWishlist) {
        for (CartItem item : loadAllItems()) {
            if (item.getUserId().equals(userId)
                    && item.getBookId().equals(bookId)
                    && item.isWishlist() == isWishlist)
                return true;
        }
        return false;
    }
}
