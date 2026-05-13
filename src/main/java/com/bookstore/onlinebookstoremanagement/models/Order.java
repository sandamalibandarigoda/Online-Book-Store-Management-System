package com.bookstore.onlinebookstoremanagement.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String username;
    private List<CartItem> items;
    private double totalAmount;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private String orderDate;
    private String shippingAddress;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Order() {}

    public Order(String orderId, String userId, String username,
                 List<CartItem> items, double totalAmount,
                 String paymentMethod, String shippingAddress) {
        this.orderId         = orderId;
        this.userId          = userId;
        this.username        = username;
        this.items           = items;
        this.totalAmount     = totalAmount;
        this.status          = "PENDING";
        this.paymentMethod   = paymentMethod;
        this.paymentStatus   = "UNPAID";
        this.orderDate       = LocalDateTime.now().format(FMT);
        this.shippingAddress = shippingAddress;
    }

    // Getters
    public String        getOrderId()         { return orderId; }
    public String        getUserId()          { return userId; }
    public String        getUsername()        { return username; }
    public List<CartItem>getItems()           { return items; }
    public double        getTotalAmount()     { return totalAmount; }
    public String        getStatus()          { return status; }
    public String        getPaymentMethod()   { return paymentMethod; }
    public String        getPaymentStatus()   { return paymentStatus; }
    public String        getOrderDate()       { return orderDate; }
    public String        getShippingAddress() { return shippingAddress; }

    // Setters
    public void setOrderId(String orderId)           { this.orderId         = orderId; }
    public void setUserId(String userId)             { this.userId          = userId; }
    public void setUsername(String username)         { this.username        = username; }
    public void setItems(List<CartItem> items)       { this.items           = items; }
    public void setTotalAmount(double totalAmount)   { this.totalAmount     = totalAmount; }
    public void setStatus(String status)             { this.status          = status; }
    public void setPaymentMethod(String method)      { this.paymentMethod   = method; }
    public void setPaymentStatus(String payStatus)   { this.paymentStatus   = payStatus; }
    public void setOrderDate(String orderDate)       { this.orderDate       = orderDate; }
    public void setShippingAddress(String address)   { this.shippingAddress = address; }

    public String toFileString() {
        StringBuilder itemStr = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CartItem it = items.get(i);
            itemStr.append(it.getBookId())
                    .append(":").append(it.getBookTitle())
                    .append(":").append(it.getQuantity())
                    .append(":").append(it.getPrice());
            if (i < items.size() - 1) itemStr.append(";");
        }
        return orderId + "|" + userId + "|" + username + "|" +
                itemStr + "|" + totalAmount + "|" + status + "|" +
                paymentMethod + "|" + paymentStatus + "|" +
                orderDate + "|" + shippingAddress;
    }

    public static Order fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        List<CartItem> items = new ArrayList<>();
        if (!p[3].isEmpty()) {
            for (String seg : p[3].split(";")) {
                String[] sp = seg.split(":", -1);
                items.add(new CartItem("", p[1], sp[0], sp[1],
                        Double.parseDouble(sp[3]),
                        Integer.parseInt(sp[2]), false));
            }
        }
        Order o = new Order(p[0], p[1], p[2], items,
                Double.parseDouble(p[4]), p[6], p[9]);
        o.setStatus(p[5]);
        o.setPaymentStatus(p[7]);
        o.setOrderDate(p[8]);
        return o;
    }
}
