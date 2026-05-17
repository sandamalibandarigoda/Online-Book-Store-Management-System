package com.bookstore.onlinebookstoremanagement.orders;

import com.bookstore.onlinebookstoremanagement.models.Order;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() {
        orderService.init();
    }

    // ── POST /api/orders ─────────────────────────────────────
    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestBody Map<String, Object> body) {

        @SuppressWarnings("unchecked")
        Map<String, String> paymentDetails =
                (Map<String, String>) body.getOrDefault(
                        "paymentDetails",
                        new java.util.HashMap<>());

        Map<String, Object> result =
                orderService.placeOrder(
                        (String) body.get("userId"),
                        (String) body.get("username"),
                        (String) body.get("shippingAddress"),
                        (String) body.get("paymentMethod"),
                        paymentDetails);

        if ((boolean) result.get("success"))
            return ResponseEntity.ok(result);
        return ResponseEntity.badRequest().body(result);
    }

    // ── GET /api/orders/my/{userId} ──────────────────────────
    @GetMapping("/my/{userId}")
    public ResponseEntity<List<Order>> getMyOrders(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                orderService.getMyOrders(userId));
    }

    // ── GET /api/orders/{orderId}/{userId} ───────────────────
    @GetMapping("/{orderId}/{userId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable String orderId,
            @PathVariable String userId) {
        Order order = orderService.getOrderById(
                orderId, userId);
        if (order != null)
            return ResponseEntity.ok(order);
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Order not found."));
    }

    // ── GET /api/orders/track/{orderId}/{userId} ─────────────
    @GetMapping("/track/{orderId}/{userId}")
    public ResponseEntity<?> trackOrder(
            @PathVariable String orderId,
            @PathVariable String userId) {
        Map<String, Object> tracking =
                orderService.trackOrder(orderId, userId);
        if (tracking != null)
            return ResponseEntity.ok(tracking);
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Order not found."));
    }

    // ── PUT /api/orders/{orderId}/cancel ─────────────────────
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String result = orderService.cancelOrder(
                orderId, body.get("userId"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ── GET /api/orders/all (admin) ──────────────────────────
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(
                orderService.getAllOrders());
    }

    // ── PUT /api/orders/{orderId}/status (admin) ─────────────
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String result = orderService.updateOrderStatus(
                orderId, body.get("status"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // ── GET /api/orders/stats (admin) ────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(
                orderService.getOrderStats());
    }
}