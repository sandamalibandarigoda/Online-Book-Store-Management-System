package com.bookstore.onlinebookstoremanagement.cart;

import com.bookstore.onlinebookstoremanagement.models.CartItem;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostConstruct
    public void init() {
        cartService.init();
    }

    // GET /api/cart/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getCart(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                cartService.getCart(userId));
    }

    // POST /api/cart
    @PostMapping
    public ResponseEntity<Map<String, String>> addToCart(
            @RequestBody Map<String, String> body) {
        int quantity;
        try {
            quantity = Integer.parseInt(
                    body.get("quantity"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid quantity."));
        }

        String result = cartService.addToCart(
                body.get("userId"),
                body.get("bookId"),
                quantity);

        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // PUT /api/cart/{cartItemId}
    @PutMapping("/{cartItemId}")
    public ResponseEntity<Map<String, String>> updateQuantity(
            @PathVariable String cartItemId,
            @RequestBody Map<String, String> body) {
        int quantity;
        try {
            quantity = Integer.parseInt(
                    body.get("quantity"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid quantity."));
        }

        String result = cartService.updateQuantity(
                body.get("userId"),
                cartItemId,
                quantity);

        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // DELETE /api/cart/{cartItemId}
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeFromCart(
            @PathVariable String cartItemId,
            @RequestBody Map<String, String> body) {
        String result = cartService.removeFromCart(
                body.get("userId"), cartItemId);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // DELETE /api/cart/clear/{userId}
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Map<String, String>> clearCart(
            @PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(
                Map.of("message",
                        "SUCCESS: Cart cleared."));
    }

    // ─── WISHLIST ENDPOINTS ──────────────────────────────────

    // GET /api/cart/wishlist/{userId}
    @GetMapping("/wishlist/{userId}")
    public ResponseEntity<List<CartItem>> getWishlist(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                cartService.getWishlist(userId));
    }

    // POST /api/cart/wishlist
    @PostMapping("/wishlist")
    public ResponseEntity<Map<String, String>> addToWishlist(
            @RequestBody Map<String, String> body) {
        String result = cartService.addToWishlist(
                body.get("userId"),
                body.get("bookId"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // DELETE /api/cart/wishlist/{cartItemId}
    @DeleteMapping("/wishlist/{cartItemId}")
    public ResponseEntity<Map<String, String>>
    removeFromWishlist(
            @PathVariable String cartItemId,
            @RequestBody Map<String, String> body) {
        String result = cartService.removeFromWishlist(
                body.get("userId"), cartItemId);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // PUT /api/cart/wishlist/move/{cartItemId}
    @PutMapping("/wishlist/move/{cartItemId}")
    public ResponseEntity<Map<String, String>> moveToCart(
            @PathVariable String cartItemId,
            @RequestBody Map<String, String> body) {
        String result = cartService.moveToCart(
                body.get("userId"), cartItemId);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }
}
