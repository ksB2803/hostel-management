package com.kripal.hostel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection manager.
 * Configure DB_URL, DB_USER, and DB_PASSWORD before running.
 */
public class DBConnection {

    // ── Change these to match your PostgreSQL setup ──────────────────────────
    private static final String DB_URL      = "jdbc:postgresql://localhost:5432/hostel_db";
    private static final String DB_USER     = "postgres";
    private static final String DB_PASSWORD = "0000";
    // ─────────────────────────────────────────────────────────────────────────

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DB] Connected to hostel_db.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found. Add it to classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    /** Returns the singleton instance (creates it on first call). */
    public static synchronized DBConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    /** Returns the raw JDBC connection. */
    public Connection getConnection() {
        return connection;
    }

    /** Closes the connection and resets the singleton. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
            instance = null;
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
