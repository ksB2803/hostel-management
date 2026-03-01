package com.kripal.hostel.dao;

import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.model.Hostel;
import com.kripal.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HostelDAO {

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    public void addHostel(Hostel h) {
        String sql = "INSERT INTO HOSTEL (hostel_name, total_rooms, warden_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getHostelName());
            ps.setInt(2, h.getTotalRooms());
            if (h.getWardenId() > 0) ps.setInt(3, h.getWardenId());
            else                     ps.setNull(3, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) h.setHostelId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addHostel failed: " + e.getMessage(), e);
        }
    }

    public List<Hostel> getAllHostels() {
        List<Hostel> list = new ArrayList<>();
        String sql = "SELECT * FROM HOSTEL ORDER BY hostel_id";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllHostels failed: " + e.getMessage(), e);
        }
        return list;
    }

    public Hostel getHostelById(int id) {
        String sql = "SELECT * FROM HOSTEL WHERE hostel_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("getHostelById failed: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean updateHostel(Hostel h) {
        String sql = "UPDATE HOSTEL SET hostel_name=?, total_rooms=?, warden_id=? WHERE hostel_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, h.getHostelName());
            ps.setInt(2, h.getTotalRooms());
            if (h.getWardenId() > 0) ps.setInt(3, h.getWardenId());
            else                     ps.setNull(3, Types.INTEGER);
            ps.setInt(4, h.getHostelId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("updateHostel failed: " + e.getMessage(), e);
        }
    }

    public boolean deleteHostel(int id) {
        String sql = "DELETE FROM HOSTEL WHERE hostel_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("deleteHostel failed: " + e.getMessage(), e);
        }
    }

    private Hostel map(ResultSet rs) throws SQLException {
        int wardenId = rs.getInt("warden_id");
        if (rs.wasNull()) wardenId = 0;
        return new Hostel(
                rs.getInt("hostel_id"),
                rs.getString("hostel_name"),
                rs.getInt("total_rooms"),
                wardenId
        );
    }
}
