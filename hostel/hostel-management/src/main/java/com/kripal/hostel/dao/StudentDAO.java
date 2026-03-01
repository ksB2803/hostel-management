package com.kripal.hostel.dao;

import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.model.Student;
import com.kripal.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    public void addStudent(Student s) {
        String sql = "INSERT INTO STUDENT (roll_no, name, branch, year, cgpa, attendance, email, gender, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString    (1, s.getRollNo());
            ps.setString    (2, s.getName());
            ps.setString    (3, s.getBranch());
            ps.setString    (4, s.getYear());
            ps.setBigDecimal(5, s.getCgpa());
            ps.setBigDecimal(6, s.getAttendance());
            ps.setString    (7, s.getEmail());
            ps.setString    (8, s.getGender() != null ? s.getGender() : "M");
            ps.setString    (9, s.getStatus() != null ? s.getStatus() : "ACTIVE");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("addStudent failed: " + e.getMessage(), e);
        }
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM STUDENT ORDER BY year, branch, roll_no");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllStudents failed: " + e.getMessage(), e);
        }
        return list;
    }

    public Student getByRollNo(String rollNo) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM STUDENT WHERE roll_no = ?")) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("getByRollNo failed: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Student> getByBranch(String branch) {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM STUDENT WHERE branch = ? ORDER BY roll_no")) {
            ps.setString(1, branch);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) {
            throw new DatabaseException("getByBranch failed: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Student> getByYear(String year) {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM STUDENT WHERE year = ? ORDER BY roll_no")) {
            ps.setString(1, year);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) {
            throw new DatabaseException("getByYear failed: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateStudent(Student s) {
        String sql = "UPDATE STUDENT SET name=?, branch=?, year=?, cgpa=?, " +
                     "attendance=?, email=?, gender=?, status=? WHERE roll_no=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString    (1, s.getName());
            ps.setString    (2, s.getBranch());
            ps.setString    (3, s.getYear());
            ps.setBigDecimal(4, s.getCgpa());
            ps.setBigDecimal(5, s.getAttendance());
            ps.setString    (6, s.getEmail());
            ps.setString    (7, s.getGender());
            ps.setString    (8, s.getStatus());
            ps.setString    (9, s.getRollNo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("updateStudent failed: " + e.getMessage(), e);
        }
    }

    public boolean deleteStudent(String rollNo) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM STUDENT WHERE roll_no = ?")) {
            ps.setString(1, rollNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("deleteStudent failed: " + e.getMessage(), e);
        }
    }

    public static Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("roll_no"),
                rs.getString("name"),
                rs.getString("branch"),
                rs.getString("year"),
                rs.getBigDecimal("cgpa"),
                rs.getBigDecimal("attendance"),
                rs.getString("email"),
                rs.getString("gender"),
                rs.getString("status")
        );
    }
}
