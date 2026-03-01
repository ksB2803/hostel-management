package com.kripal.hostel.dao;

import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.model.User;
import com.kripal.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    /** Returns the User if username+password match, otherwise null. */
    public User login(String username, String password) {
        String sql = "SELECT * FROM USERS WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("login failed: " + e.getMessage(), e);
        }
        return null;
    }

    public void addUser(User u) {
        String sql = "INSERT INTO USERS (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setUserId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addUser failed: " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM USERS ORDER BY user_id";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllUsers failed: " + e.getMessage(), e);
        }
        return list;
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
        );
    }
}
