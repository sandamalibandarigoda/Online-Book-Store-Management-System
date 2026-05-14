package com.bookstore.onlinebookstoremanagement.orders;

import com.bookstore.onlinebookstoremanagement.models.Order;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderFileManager {

    private static final String FILE_PATH = "data/orders.txt";

    public void init() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating orders file: "
                    + e.getMessage());
        }
    }

    public List<Order> loadAllOrders() {
        List<Order> orders = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty())
                    orders.add(Order.fromFileString(line));
            }
        } catch (IOException e) {
            System.out.println("Error reading orders: "
                    + e.getMessage());
        }
        return orders;
    }

    public void saveAllOrders(List<Order> orders) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH))) {
            for (Order o : orders) {
                bw.write(o.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving orders: "
                    + e.getMessage());
        }
    }

    public void appendOrder(Order order) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            bw.write(order.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error appending order: "
                    + e.getMessage());
        }
    }

    // Get orders for a specific user
    public List<Order> loadOrdersByUser(String userId) {
        List<Order> result = new ArrayList<>();
        for (Order o : loadAllOrders()) {
            if (o.getUserId().equals(userId))
                result.add(o);
        }
        return result;
    }

    // Get order by ID
    public Order getOrderById(String orderId) {
        for (Order o : loadAllOrders()) {
            if (o.getOrderId().equals(orderId)) return o;
        }
        return null;
    }

    // Get orders by status
    public List<Order> loadOrdersByStatus(String status) {
        List<Order> result = new ArrayList<>();
        for (Order o : loadAllOrders()) {
            if (o.getStatus().equalsIgnoreCase(status))
                result.add(o);
        }
        return result;
    }
}
