package com.bookstore.onlinebookstoremanagement.auth;

import com.bookstore.onlinebookstoremanagement.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserFileManager userFileManager;

    // Initialize file on startup
    public void init() {
        userFileManager.init();
    }

    // Register
    public String register(String username, String email,
                           String password, String role) {
        if (username == null || username.trim().isEmpty())
            return "ERROR: Username cannot be empty.";
        if (email == null || !email.contains("@"))
            return "ERROR: Invalid email address.";
        // Password validation
        if (password == null || password.length() < 6)
            return "ERROR: Password must be at least 6 characters.";
        
        // Note: Admin code validation is done in frontend only
        // Backend saves the actual password for login
        
        if (userFileManager.usernameExists(username))
            return "ERROR: Username already taken.";
        if (userFileManager.emailExists(email))
            return "ERROR: Email already registered.";

        String userId = UUID.randomUUID()
                .toString().substring(0, 8);
        User newUser  = new User(userId, username, email,
                password,
                (role == null || role.isEmpty())
                        ? "customer" : role);
        userFileManager.appendUser(newUser);
        return "SUCCESS: Registered! Your ID is " + userId;
    }

    // Login
    public User login(String username, String password) {
        for (User user : userFileManager.loadAllUsers()) {
            if (user.getUsername().equalsIgnoreCase(username)
                    && user.getPassword().equals(password))
                return user;
        }
        return null;
    }

    // Update profile
    public String updateProfile(String userId,
                                String fullName,
                                String address,
                                String phone,
                                String email) {
        List<User> users = userFileManager.loadAllUsers();
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                if (fullName != null && !fullName.isEmpty())
                    user.setFullName(fullName);
                if (address != null && !address.isEmpty())
                    user.setAddress(address);
                if (phone != null && !phone.isEmpty())
                    user.setPhone(phone);
                if (email != null && email.contains("@"))
                    user.setEmail(email);
                userFileManager.saveAllUsers(users);
                return "SUCCESS: Profile updated.";
            }
        }
        return "ERROR: User not found.";
    }

    // Change password
    public String changePassword(String userId,
                                 String oldPassword,
                                 String newPassword) {
        List<User> users = userFileManager.loadAllUsers();
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                if (!user.getPassword().equals(oldPassword))
                    return "ERROR: Old password incorrect.";
                if (newPassword == null
                        || newPassword.length() < 6)
                    return "ERROR: Min 6 characters required.";
                user.setPassword(newPassword);
                userFileManager.saveAllUsers(users);
                return "SUCCESS: Password changed.";
            }
        }
        return "ERROR: User not found.";
    }

    // Forgot password
    public String forgotPassword(String username,
                                 String email,
                                 String newPassword) {
        if (newPassword == null || newPassword.length() < 6)
            return "ERROR: Min 6 characters required.";
        List<User> users = userFileManager.loadAllUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)
                    && user.getEmail().equalsIgnoreCase(email)) {
                user.setPassword(newPassword);
                userFileManager.saveAllUsers(users);
                return "SUCCESS: Password reset successfully.";
            }
        }
        return "ERROR: Username and email do not match.";
    }

    // Get user by ID
    public User getUserById(String userId) {
        for (User user : userFileManager.loadAllUsers()) {
            if (user.getUserId().equals(userId))
                return user;
        }
        return null;
    }

    // Get all users (for admin)
    public List<User> getAllUsers() {
        return userFileManager.loadAllUsers();
    }

    // Delete user
    public String deleteUser(String userId) {
        List<User> users = userFileManager.loadAllUsers();
        boolean removed = users.removeIf(
                u -> u.getUserId().equals(userId));
        if (!removed) return "ERROR: User not found.";
        userFileManager.saveAllUsers(users);
        return "SUCCESS: User deleted.";
    }

    // Save all users - used by Admin promote feature
    public void saveAllUsers(List<User> users) {
        userFileManager.saveAllUsers(users);
    }
}
