package com.kripal.hostel.dao;

import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.exception.RoomFullException;
import com.kripal.hostel.model.Allotment;
import com.kripal.hostel.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AllotmentDAO {

    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    // ── ASSIGN ROOM — full JDBC Transaction ───────────────────────────────────
    public void assignRoom(String rollNo, int roomId) throws RoomFullException {
        Connection conn = conn();
        try {
            conn.setAutoCommit(false);

            // Row-lock the room and check capacity
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT capacity, occupied FROM ROOM WHERE room_id = ? FOR UPDATE")) {
                check.setInt(1, roomId);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); throw new DatabaseException("Room not found: " + roomId); }
                    if (rs.getInt("occupied") >= rs.getInt("capacity")) {
                        conn.rollback();
                        throw new RoomFullException("Room " + roomId + " is full");
                    }
                }
            }

            // Insert allotment
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO ALLOTMENT (roll_no, room_id, allotment_date, status) VALUES (?, ?, ?, 'ACTIVE')")) {
                ins.setString(1, rollNo);
                ins.setInt   (2, roomId);
                ins.setDate  (3, Date.valueOf(LocalDate.now()));
                ins.executeUpdate();
            }

            // Increment occupied
            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE ROOM SET occupied = occupied + 1 WHERE room_id = ?")) {
                upd.setInt(1, roomId);
                upd.executeUpdate();
            }

            conn.commit();
        } catch (RoomFullException e) {
            safeRollback(conn); throw e;
        } catch (SQLException e) {
            safeRollback(conn);
            throw new DatabaseException("assignRoom failed: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ── REMOVE ALLOTMENT — Transaction ────────────────────────────────────────
    public void removeAllotment(int allotmentId) {
        Connection conn = conn();
        try {
            conn.setAutoCommit(false);

            int roomId;
            try (PreparedStatement find = conn.prepareStatement(
                    "SELECT room_id FROM ALLOTMENT WHERE allotment_id = ? AND status = 'ACTIVE'")) {
                find.setInt(1, allotmentId);
                try (ResultSet rs = find.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); throw new DatabaseException("Active allotment not found: " + allotmentId); }
                    roomId = rs.getInt("room_id");
                }
            }

            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE ALLOTMENT SET status='LEFT', checkout_date=? WHERE allotment_id=?")) {
                upd.setDate(1, Date.valueOf(LocalDate.now()));
                upd.setInt (2, allotmentId);
                upd.executeUpdate();
            }

            try (PreparedStatement dec = conn.prepareStatement(
                    "UPDATE ROOM SET occupied = GREATEST(occupied - 1, 0) WHERE room_id = ?")) {
                dec.setInt(1, roomId);
                dec.executeUpdate();
            }

            conn.commit();
        } catch (DatabaseException e) {
            safeRollback(conn); throw e;
        } catch (SQLException e) {
            safeRollback(conn);
            throw new DatabaseException("removeAllotment failed: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public Allotment getStudentRoom(String rollNo) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM ALLOTMENT WHERE roll_no = ? AND status = 'ACTIVE' " +
                "ORDER BY allotment_date DESC LIMIT 1")) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("getStudentRoom failed: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Allotment> getAllAllotments() {
        List<Allotment> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM ALLOTMENT ORDER BY allotment_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllAllotments failed: " + e.getMessage(), e);
        }
        return list;
    }

    private Allotment map(ResultSet rs) throws SQLException {
        Date co = rs.getDate("checkout_date");
        return new Allotment(
                rs.getInt   ("allotment_id"),
                rs.getString("roll_no"),
                rs.getInt   ("room_id"),
                rs.getDate  ("allotment_date").toLocalDate(),
                co != null ? co.toLocalDate() : null,
                rs.getString("status")
        );
    }

    private void safeRollback(Connection c) { try { c.rollback(); } catch (SQLException ignored) {} }
}
