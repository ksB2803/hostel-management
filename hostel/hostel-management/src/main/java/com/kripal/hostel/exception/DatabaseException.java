package com.kripal.hostel.exception;

/** Thrown when a JDBC / database operation fails. */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
