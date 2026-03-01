package com.kripal.hostel.exception;

/** Thrown when a room has no remaining capacity for a new allotment. */
public class RoomFullException extends Exception {
    public RoomFullException(String message) {
        super(message);
    }
}
