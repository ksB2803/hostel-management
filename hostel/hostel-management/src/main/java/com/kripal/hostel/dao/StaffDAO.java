package com.kripal.hostel.dao;

import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.model.Staff;
import com.kripal.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    public void addStaff(Staff s) {
        String sql = "INSERT INTO STAFF (name, role, hostel_id, shift, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getRole());
            if (s.getHostelId() > 0) ps.setInt(3, s.getHostelId());
            else                     ps.setNull(3, Types.INTEGER);
            ps.setString(4, s.getShift());
            ps.setString(5, s.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setStaffId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addStaff failed: " + e.getMessage(), e);
        }
    }

    public List<Staff> getAllStaff() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM STAFF ORDER BY staff_id";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllStaff failed: " + e.getMessage(), e);
        }
        return list;
    }

    public Staff getStaffById(int id) {
        String sql = "SELECT * FROM STAFF WHERE staff_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("getStaffById failed: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Staff> getStaffByHostel(int hostelId) {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM STAFF WHERE hostel_id = ? ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, hostelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getStaffByHostel failed: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateStaff(Staff s) {
        String sql = "UPDATE STAFF SET name=?, role=?, hostel_id=?, shift=?, phone=? WHERE staff_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getRole());
            if (s.getHostelId() > 0) ps.setInt(3, s.getHostelId());
            else                     ps.setNull(3, Types.INTEGER);
            ps.setString(4, s.getShift());
            ps.setString(5, s.getPhone());
            ps.setInt(6, s.getStaffId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("updateStaff failed: " + e.getMessage(), e);
        }
    }

    public boolean deleteStaff(int id) {
        String sql = "DELETE FROM STAFF WHERE staff_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("deleteStaff failed: " + e.getMessage(), e);
        }
    }

    private Staff map(ResultSet rs) throws SQLException {
        int hostelId = rs.getInt("hostel_id");
        if (rs.wasNull()) hostelId = 0;
        return new Staff(
                rs.getInt("staff_id"),
                rs.getString("name"),
                rs.getString("role"),
                hostelId,
                rs.getString("shift"),
                rs.getString("phone")
        );
    }
}
