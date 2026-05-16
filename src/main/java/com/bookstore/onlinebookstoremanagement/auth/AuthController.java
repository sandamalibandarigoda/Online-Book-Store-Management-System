package com.bookstore.onlinebookstoremanagement.auth;

import com.bookstore.onlinebookstoremanagement.models.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Initialize file on startup
    @PostConstruct
    public void init() {
        authService.init();
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody User user) {
        String result = authService.register(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole());
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body) {
        User user = authService.login(
                body.get("username"),
                body.get("password"));
        if (user != null) {
            user.setPassword(null); // hide password
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Invalid username or password."));
    }

    // GET /api/auth/profile/{userId}
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(
            @PathVariable String userId) {
        User user = authService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest()
                .body(Map.of("error", "User not found."));
    }

    // PUT /api/auth/profile/{userId}
    @PutMapping("/profile/{userId}")
    public ResponseEntity<Map<String, String>> updateProfile(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        String result = authService.updateProfile(
                userId,
                body.get("fullName"),
                body.get("address"),
                body.get("phone"),
                body.get("email"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // PUT /api/auth/change-password/{userId}
    @PutMapping("/change-password/{userId}")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        String result = authService.changePassword(
                userId,
                body.get("oldPassword"),
                body.get("newPassword"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> body) {
        String result = authService.forgotPassword(
                body.get("username"),
                body.get("email"),
                body.get("newPassword"));
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }

    // GET /api/auth/users (admin only)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    // DELETE /api/auth/users/{userId}
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable String userId) {
        String result = authService.deleteUser(userId);
        if (result.startsWith("SUCCESS"))
            return ResponseEntity.ok(
                    Map.of("message", result));
        return ResponseEntity.badRequest()
                .body(Map.of("error", result));
    }
}
