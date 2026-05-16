package com.bookstore.onlinebookstoremanagement.orders;

import com.bookstore.onlinebookstoremanagement.cart.CartService;
import com.bookstore.onlinebookstoremanagement.catalog.BookService;
import com.bookstore.onlinebookstoremanagement.models.Book;
import com.bookstore.onlinebookstoremanagement.models.CartItem;
import com.bookstore.onlinebookstoremanagement.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderFileManager orderFileManager;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    @Autowired
    private PaymentService paymentService;

    public void init() {
        orderFileManager.init();
    }

    // Place a new order
    public Map<String, Object> placeOrder(
            String userId,
            String username,
            String shippingAddress,
            String paymentMethod,
            Map<String, String> paymentDetails) {

        Map<String, Object> response = new HashMap<>();

        // Validate cart
        List<CartItem> cartItems =
                cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            response.put("success", false);
            response.put("message",
                    "ERROR: Cart is empty.");
            return response;
        }

        // Validate address
        if (shippingAddress == null
                || shippingAddress.trim().isEmpty()) {
            response.put("success", false);
            response.put("message",
                    "ERROR: Shipping address required.");
            return response;
        }

        // Validate payment method
        if (!paymentService.isValidMethod(paymentMethod)) {
            response.put("success", false);
            response.put("message",
                    "ERROR: Invalid payment method.");
            return response;
        }

        // Validate stock for all items
        List<Book> allBooks = bookService.getAllBooks();
        for (CartItem item : cartItems) {
            for (Book book : allBooks) {
                if (book.getBookId()
                        .equals(item.getBookId())) {
                    if (book.getStock() < item.getQuantity()) {
                        response.put("success", false);
                        response.put("message",
                                "ERROR: Not enough stock for \""
                                        + book.getTitle() + "\". Only "
                                        + book.getStock() + " left.");
                        return response;
                    }
                }
            }
        }

        // Calculate total
        double total = cartService.getCartTotal(userId);

        // Process payment
        Map<String, Object> payResult =
                paymentService.processPayment(
                        paymentMethod, total, paymentDetails);

        if (!(boolean) payResult.get("success")) {
            response.put("success", false);
            response.put("message", payResult.get("message"));
            return response;
        }

        // Create order
        String orderId = "ORD-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, userId, username,
                cartItems, total,
                paymentMethod, shippingAddress);

        // Set payment status
        if (!paymentMethod.equalsIgnoreCase(
                "CASH_ON_DELIVERY")) {
            order.setPaymentStatus("PAID");
        }
        order.setStatus("CONFIRMED");

        // Deduct stock from books
        for (CartItem item : cartItems) {
            for (Book book : allBooks) {
                if (book.getBookId()
                        .equals(item.getBookId())) {
                    book.setStock(book.getStock()
                            - item.getQuantity());
                }
            }
        }
        bookService.saveAllBooks(allBooks);

        // Save order and clear cart
        orderFileManager.appendOrder(order);
        cartService.clearCart(userId);

        // Build response
        response.put("success", true);
        response.put("message",
                "SUCCESS: Order placed successfully!");
        response.put("orderId", orderId);
        response.put("totalAmount", total);
        response.put("paymentStatus",
                order.getPaymentStatus());
        response.put("orderStatus", order.getStatus());
        response.put("transactionId",
                payResult.getOrDefault(
                        "transactionId", "N/A"));
        return response;
    }

    // Get all orders for a user
    public List<Order> getMyOrders(String userId) {
        return orderFileManager.loadOrdersByUser(userId);
    }

    // Get order by ID
    public Order getOrderById(String orderId,
                              String userId) {
        Order order = orderFileManager
                .getOrderById(orderId);
        if (order == null) return null;
        if (!order.getUserId().equals(userId)) return null;
        return order;
    }

    // Track order status
    public Map<String, Object> trackOrder(
            String orderId, String userId) {
        Order order = orderFileManager
                .getOrderById(orderId);
        if (order == null
                || !order.getUserId().equals(userId))
            return null;

        Map<String, Object> tracking = new HashMap<>();
        tracking.put("orderId", orderId);
        tracking.put("status", order.getStatus());
        tracking.put("paymentStatus",
                order.getPaymentStatus());
        tracking.put("orderDate", order.getOrderDate());
        tracking.put("shippingAddress",
                order.getShippingAddress());
        tracking.put("progressStep",
                getProgressStep(order.getStatus()));
        tracking.put("totalAmount",
                order.getTotalAmount());
        return tracking;
    }

    // Cancel an order
    public String cancelOrder(String orderId,
                              String userId) {
        List<Order> orders =
                orderFileManager.loadAllOrders();
        for (Order o : orders) {
            if (o.getOrderId().equals(orderId)) {
                if (!o.getUserId().equals(userId))
                    return "ERROR: Access denied.";
                if (o.getStatus().equals("SHIPPED") ||
                        o.getStatus().equals("DELIVERED"))
                    return "ERROR: Cannot cancel a "
                            + o.getStatus() + " order.";

                o.setStatus("CANCELLED");

                // Restore stock
                List<Book> allBooks =
                        bookService.getAllBooks();
                for (CartItem item : o.getItems()) {
                    for (Book book : allBooks) {
                        if (book.getBookId().equals(
                                item.getBookId())) {
                            book.setStock(book.getStock()
                                    + item.getQuantity());
                        }
                    }
                }
                bookService.saveAllBooks(allBooks);

                // Mark refund if paid
                if (o.getPaymentStatus().equals("PAID"))
                    o.setPaymentStatus("REFUNDED");

                orderFileManager.saveAllOrders(orders);
                return "SUCCESS: Order " + orderId
                        + " cancelled.";
            }
        }
        return "ERROR: Order not found.";
    }

    // Get all orders (admin)
    public List<Order> getAllOrders() {
        return orderFileManager.loadAllOrders();
    }

    // Update order status (admin)
    public String updateOrderStatus(String orderId,
                                    String newStatus) {
        List<String> validStatuses = List.of(
                "PENDING", "CONFIRMED",
                "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(
                newStatus.toUpperCase()))
            return "ERROR: Invalid status.";

        List<Order> orders =
                orderFileManager.loadAllOrders();
        for (Order o : orders) {
            if (o.getOrderId().equals(orderId)) {
                o.setStatus(newStatus.toUpperCase());
                orderFileManager.saveAllOrders(orders);
                return "SUCCESS: Order status updated to "
                        + newStatus.toUpperCase();
            }
        }
        return "ERROR: Order not found.";
    }

    // Get order statistics (admin)
    public Map<String, Object> getOrderStats() {
        List<Order> orders =
                orderFileManager.loadAllOrders();
        double revenue = 0;
        int pending = 0, confirmed = 0,
                shipped = 0, delivered = 0, cancelled = 0;

        for (Order o : orders) {
            switch (o.getStatus()) {
                case "PENDING"   -> pending++;
                case "CONFIRMED" -> confirmed++;
                case "SHIPPED"   -> shipped++;
                case "DELIVERED" -> {
                    delivered++;
                    revenue += o.getTotalAmount();
                }
                case "CANCELLED" -> cancelled++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orders.size());
        stats.put("pending", pending);
        stats.put("confirmed", confirmed);
        stats.put("shipped", shipped);
        stats.put("delivered", delivered);
        stats.put("cancelled", cancelled);
        stats.put("totalRevenue",
                Math.round(revenue * 100.0) / 100.0);
        return stats;
    }

    // Save all books helper
    private int getProgressStep(String status) {
        return switch (status) {
            case "PENDING"   -> 1;
            case "CONFIRMED" -> 2;
            case "SHIPPED"   -> 3;
            case "DELIVERED" -> 4;
            default          -> 0;
        };
    }
}