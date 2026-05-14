package com.bookstore.onlinebookstoremanagement.auth;

import com.bookstore.onlinebookstoremanagement.models.User;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserFileManager {

    private static final String FILE_PATH = "data/users.txt";

    public void init() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating users file: "
                    + e.getMessage());
        }
    }

    public List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty())
                    users.add(User.fromFileString(line));
            }
        } catch (IOException e) {
            System.out.println("Error reading users: "
                    + e.getMessage());
        }
        return users;
    }

    public void saveAllUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH))) {
            for (User user : users) {
                bw.write(user.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: "
                    + e.getMessage());
        }
    }

    public void appendUser(User user) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            bw.write(user.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error appending user: "
                    + e.getMessage());
        }
    }

    public boolean usernameExists(String username) {
        for (User u : loadAllUsers()) {
            if (u.getUsername().equalsIgnoreCase(username))
                return true;
        }
        return false;
    }

    public boolean emailExists(String email) {
        for (User u : loadAllUsers()) {
            if (u.getEmail().equalsIgnoreCase(email))
                return true;
        }
        return false;
    }
}
