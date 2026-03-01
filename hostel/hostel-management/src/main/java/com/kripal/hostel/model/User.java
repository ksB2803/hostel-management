package com.kripal.hostel.model;

public class User {
    private int    userId;
    private String username;
    private String password;
    private String role;   // ADMIN / USER

    public User() {}

    public User(int userId, String username, String password, String role) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    public int    getUserId()  { return userId; }
    public String getUsername(){ return username; }
    public String getPassword(){ return password; }
    public String getRole()    { return role; }

    public void setUserId  (int userId)      { this.userId   = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole    (String role)     { this.role     = role; }

    @Override
    public String toString() {
        return String.format("User[id=%d, username=%s, role=%s]", userId, username, role);
    }
}
