package com.kripal.hostel.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Utility to insert audit log entries into the LOGS table. */
public class LogUtil {

    private static final String INSERT_LOG =
            "INSERT INTO LOGS (action, timestamp, username) VALUES (?, NOW(), ?)";

    public static void log(String action, String username) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_LOG)) {
                ps.setString(1, action);
                ps.setString(2, username);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[LOG] Failed to write log: " + e.getMessage());
        }
    }
}
