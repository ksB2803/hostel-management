package com.kripal.hostel.dao;

import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.model.Room;
import com.kripal.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    public void addRoom(Room r) {
        String sql = "INSERT INTO ROOM (hostel_id, room_number, room_type, capacity, occupied) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getHostelId());
            ps.setString(2, r.getRoomNumber());
            ps.setString(3, r.getRoomType());
            ps.setInt(4, r.getCapacity());
            ps.setInt(5, r.getOccupied());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setRoomId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addRoom failed: " + e.getMessage(), e);
        }
    }

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM ROOM ORDER BY hostel_id, room_number";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllRooms failed: " + e.getMessage(), e);
        }
        return list;
    }

    public Room getRoomById(int id) {
        String sql = "SELECT * FROM ROOM WHERE room_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("getRoomById failed: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Room> getRoomsByHostel(int hostelId) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM ROOM WHERE hostel_id = ? ORDER BY room_number";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, hostelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getRoomsByHostel failed: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Room> getAvailableRooms(int hostelId) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM ROOM WHERE hostel_id = ? AND occupied < capacity ORDER BY room_number";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, hostelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getAvailableRooms failed: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateRoom(Room r) {
        String sql = "UPDATE ROOM SET hostel_id=?, room_number=?, room_type=?, capacity=?, occupied=? " +
                     "WHERE room_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, r.getHostelId());
            ps.setString(2, r.getRoomNumber());
            ps.setString(3, r.getRoomType());
            ps.setInt(4, r.getCapacity());
            ps.setInt(5, r.getOccupied());
            ps.setInt(6, r.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("updateRoom failed: " + e.getMessage(), e);
        }
    }

    public boolean deleteRoom(int id) {
        String sql = "DELETE FROM ROOM WHERE room_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("deleteRoom failed: " + e.getMessage(), e);
        }
    }

    private Room map(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getInt("hostel_id"),
                rs.getString("room_number"),
                rs.getString("room_type"),
                rs.getInt("capacity"),
                rs.getInt("occupied")
        );
    }
}
