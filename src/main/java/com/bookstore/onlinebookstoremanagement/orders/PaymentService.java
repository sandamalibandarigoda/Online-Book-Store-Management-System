package com.bookstore.onlinebookstoremanagement.orders;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class PaymentService {

    // Simulate payment gateway processing
    public Map<String, Object> processPayment(
            String method,
            double amount,
            Map<String, String> paymentDetails) {

        if (method == null || method.isEmpty())
            return Map.of("success", false,
                    "message", "ERROR: No payment method.");

        return switch (method.toUpperCase()) {
            case "CREDIT_CARD" ->
                    processCreditCard(amount, paymentDetails);
            case "DEBIT_CARD"  ->
                    processDebitCard(amount, paymentDetails);
            case "PAYPAL"      ->
                    processPayPal(amount, paymentDetails);
            case "STRIPE"      ->
                    processStripe(amount, paymentDetails);
            case "CASH_ON_DELIVERY" ->
                    Map.of("success", true,
                            "message",
                            "Cash on Delivery selected. " +
                                    "Pay upon receiving.");
            default ->
                    Map.of("success", false,
                            "message",
                            "ERROR: Unknown payment method.");
        };
    }

    private Map<String, Object> processCreditCard(
            double amount,
            Map<String, String> details) {
        String card = details.getOrDefault(
                "cardNumber", "").replaceAll(" ", "");
        String cvv  = details.getOrDefault("cvv", "");

        if (card.length() != 16 || !card.matches("\\d+"))
            return Map.of("success", false,
                    "message",
                    "ERROR: Invalid card number.");
        if (cvv.length() != 3 || !cvv.matches("\\d+"))
            return Map.of("success", false,
                    "message", "ERROR: Invalid CVV.");

        // Simulate Stripe/Braintree API call
        return Map.of("success", true,
                "message",
                "Credit Card payment approved! " +
                        "Amount: $" + amount,
                "transactionId", generateTransactionId());
    }

    private Map<String, Object> processDebitCard(
            double amount,
            Map<String, String> details) {
        String card = details.getOrDefault(
                "cardNumber", "").replaceAll(" ", "");
        String pin  = details.getOrDefault("pin", "");

        if (card.length() != 16 || !card.matches("\\d+"))
            return Map.of("success", false,
                    "message",
                    "ERROR: Invalid card number.");
        if (pin.length() != 4 || !pin.matches("\\d+"))
            return Map.of("success", false,
                    "message", "ERROR: Invalid PIN.");

        return Map.of("success", true,
                "message",
                "Debit Card payment approved! " +
                        "Amount: $" + amount,
                "transactionId", generateTransactionId());
    }

    private Map<String, Object> processPayPal(
            double amount,
            Map<String, String> details) {
        String email = details.getOrDefault(
                "paypalEmail", "");

        if (!email.contains("@"))
            return Map.of("success", false,
                    "message",
                    "ERROR: Invalid PayPal email.");

        return Map.of("success", true,
                "message",
                "PayPal payment approved! " +
                        "Amount: $" + amount,
                "transactionId", generateTransactionId());
    }

    private Map<String, Object> processStripe(
            double amount,
            Map<String, String> details) {
        String token = details.getOrDefault(
                "stripeToken", "");

        if (token.isEmpty())
            return Map.of("success", false,
                    "message",
                    "ERROR: Invalid Stripe token.");

        return Map.of("success", true,
                "message",
                "Stripe payment approved! " +
                        "Amount: $" + amount,
                "transactionId", generateTransactionId());
    }

    // Generate a fake transaction ID
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis();
    }

    // Validate payment method string
    public boolean isValidMethod(String method) {
        return method != null && java.util.List.of(
                        "CREDIT_CARD", "DEBIT_CARD",
                        "PAYPAL", "STRIPE", "CASH_ON_DELIVERY")
                .contains(method.toUpperCase());
    }
}
