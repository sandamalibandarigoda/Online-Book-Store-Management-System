package com.bookstore.onlinebookstoremanagement.models;

public class User {
    private String userId;
    private String username;
    private String email;
    private String password;
    private String role;
    private String fullName;
    private String address;
    private String phone;

    public User() {}

    public User(String userId, String username, String email,
                String password, String role) {
        this.userId   = userId;
        this.username = username;
        this.email    = email;
        this.password = password;
        this.role     = role;
        this.fullName = "";
        this.address  = "";
        this.phone    = "";
    }

    // Getters
    public String getUserId()   { return userId; }
    public String getUsername() { return username; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }
    public String getFullName() { return fullName; }
    public String getAddress()  { return address; }
    public String getPhone()    { return phone; }

    // Setters
    public void setUserId(String userId)     { this.userId   = userId; }
    public void setUsername(String u)        { this.username = u; }
    public void setEmail(String email)       { this.email    = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role)         { this.role     = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setAddress(String address)   { this.address  = address; }
    public void setPhone(String phone)       { this.phone    = phone; }

    public String toFileString() {
        return userId + "|" + username + "|" + email + "|" +
                password + "|" + role + "|" + fullName + "|" +
                address + "|" + phone;
    }

    public static User fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        User u = new User(p[0], p[1], p[2], p[3], p[4]);
        u.setFullName(p[5]);
        u.setAddress(p[6]);
        u.setPhone(p[7]);
        return u;
    }
}
