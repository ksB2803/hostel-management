package com.kripal.hostel;

import com.kripal.hostel.dao.*;
import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.model.*;
import com.kripal.hostel.service.AllotmentService;
import com.kripal.hostel.service.LoginService;
import com.kripal.hostel.service.XMLImportService;
import com.kripal.hostel.util.DBConnection;
import com.kripal.hostel.util.LogUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class Main {

    private static final String XML_IMPORT_FOLDER = "student_data";

    private static final LoginService     loginService     = new LoginService();
    private static final AllotmentService allotmentService = new AllotmentService();
    private static final XMLImportService xmlService       = new XMLImportService();
    private static final StudentDAO       studentDAO       = new StudentDAO();
    private static final HostelDAO        hostelDAO        = new HostelDAO();
    private static final RoomDAO          roomDAO          = new RoomDAO();
    private static final StaffDAO         staffDAO         = new StaffDAO();
    private static final AllotmentDAO     allotmentDAO     = new AllotmentDAO();

    private static final Scanner sc = new Scanner(System.in);
    private static String loggedInUser = "system";

    public static void main(String[] args) {
        banner();
        autoImportOnStartup();

        String role = null;
        while (role == null) {
            System.out.print("\nUsername: "); String u = sc.nextLine().trim();
            System.out.print("Password: "); String p = sc.nextLine().trim();
            role = loginService.authenticate(u, p);
            if (role == null) System.out.println("  Invalid credentials.");
            else { loggedInUser = u; System.out.println("  Welcome! Role: " + role); LogUtil.log("Login", u); }
        }

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Choice: ");
            try {
                int c = Integer.parseInt(sc.nextLine().trim());
                switch (c) {
                    case 1  -> addStudent();
                    case 2  -> updateStudent();
                    case 3  -> deleteStudent();
                    case 4  -> addHostel();
                    case 5  -> addRoom();
                    case 6  -> addStaff();
                    case 7  -> autoAllotment();
                    case 8  -> showAllStudents();
                    case 9  -> showAllRooms();
                    case 10 -> showHostelStatus();
                    case 11 -> searchStudentRoom();
                    case 12 -> importXMLManual();
                    case 13 -> viewLogs();
                    case 14 -> showDayScholars();
                    case 15 -> running = false;
                    default -> System.out.println("  Unknown option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  Enter a number.");
            } catch (DatabaseException e) {
                System.out.println("  [DB Error] " + e.getMessage());
            }
        }
        System.out.println("\nGoodbye!");
        DBConnection.getInstance().close();
    }

    private static void autoImportOnStartup() {
        java.io.File folder = new java.io.File(XML_IMPORT_FOLDER);
        if (!folder.exists()) {
            System.out.println("[Startup] No 'student_data' folder found — skipping auto-import.");
            return;
        }
        System.out.println("[Startup] Importing from: " + folder.getAbsolutePath());
        int n = xmlService.importFolder(XML_IMPORT_FOLDER, "system");
        System.out.println("[Startup] Total students loaded: " + n + "\n");
    }

    // ── [1] Add Student ───────────────────────────────────────────────────────
    private static void addStudent() {
        System.out.println("\n--- Add Student ---");
        Student s = new Student();
        s.setRollNo    (prompt("Roll No (e.g. FE-IT-A-001): "));
        s.setName      (prompt("Name: "));
        s.setBranch    (prompt("Branch: "));
        s.setYear      (prompt("Year (FE/SE/TE/BE): "));
        s.setCgpa      (new BigDecimal(prompt("CGPA: ")));
        s.setAttendance(new BigDecimal(prompt("Attendance %: ")));
        s.setEmail     (prompt("Email: "));
        s.setGender    (prompt("Gender (M/F): ").toUpperCase());
        s.setStatus    ("ACTIVE");
        studentDAO.addStudent(s);
        System.out.println("  Added: " + s.getRollNo());
        LogUtil.log("Added student: " + s.getRollNo(), loggedInUser);
    }

    // ── [2] Update Student ────────────────────────────────────────────────────
    private static void updateStudent() {
        System.out.println("\n--- Update Student ---");
        String roll = prompt("Roll No: ");
        Student s = studentDAO.getByRollNo(roll);
        if (s == null) { System.out.println("  Not found."); return; }
        System.out.println("  Current: " + s);
        s.setName      (promptOr("Name (blank=keep): ",       s.getName()));
        s.setBranch    (promptOr("Branch (blank=keep): ",     s.getBranch()));
        s.setYear      (promptOr("Year (blank=keep): ",       s.getYear()));
        String cgpa = promptOr("CGPA (blank=keep): ", "");
        if (!cgpa.isEmpty()) s.setCgpa(new BigDecimal(cgpa));
        String att = promptOr("Attendance (blank=keep): ", "");
        if (!att.isEmpty()) s.setAttendance(new BigDecimal(att));
        s.setEmail (promptOr("Email (blank=keep): ",          s.getEmail()));
        s.setGender(promptOr("Gender M/F (blank=keep): ",     s.getGender()));
        s.setStatus(promptOr("Status (blank=keep): ",         s.getStatus()));
        System.out.println(studentDAO.updateStudent(s) ? "  Updated." : "  Failed.");
        LogUtil.log("Updated: " + roll, loggedInUser);
    }

    // ── [3] Delete Student ────────────────────────────────────────────────────
    private static void deleteStudent() {
        System.out.println("\n--- Delete Student ---");
        String roll = prompt("Roll No: ");
        System.out.println(studentDAO.deleteStudent(roll) ? "  Deleted." : "  Not found.");
        LogUtil.log("Deleted: " + roll, loggedInUser);
    }

    // ── [4] Add Hostel ────────────────────────────────────────────────────────
    private static void addHostel() {
        System.out.println("\n--- Add Hostel ---");
        Hostel h = new Hostel();
        h.setHostelName(prompt("Name: "));
        h.setTotalRooms(intPrompt("Total Rooms: "));
        hostelDAO.addHostel(h);
        System.out.println("  Added hostel ID: " + h.getHostelId());
        LogUtil.log("Added hostel: " + h.getHostelName(), loggedInUser);
    }

    // ── [5] Add Room ──────────────────────────────────────────────────────────
    private static void addRoom() {
        System.out.println("\n--- Add Room ---");
        Room r = new Room();
        r.setHostelId  (intPrompt("Hostel ID: "));
        r.setRoomNumber(prompt("Room Number: "));
        r.setRoomType  (prompt("Type (SINGLE/DOUBLE/TRIPLE): "));
        r.setCapacity  (intPrompt("Capacity: "));
        r.setOccupied  (0);
        roomDAO.addRoom(r);
        System.out.println("  Added room: " + r.getRoomNumber());
        LogUtil.log("Added room: " + r.getRoomNumber(), loggedInUser);
    }

    // ── [6] Add Staff ─────────────────────────────────────────────────────────
    private static void addStaff() {
        System.out.println("\n--- Add Staff ---");
        Staff s = new Staff();
        s.setName    (prompt("Name: "));
        s.setRole    (prompt("Role (WARDEN/ATTENDANT/GUARD): "));
        s.setHostelId(intPrompt("Hostel ID: "));
        s.setShift   (prompt("Shift: "));
        s.setPhone   (prompt("Phone: "));
        staffDAO.addStaff(s);
        System.out.println("  Added staff: " + s.getName());
        LogUtil.log("Added staff: " + s.getName(), loggedInUser);
    }

    // ── [7] Auto Allotment ────────────────────────────────────────────────────
    private static void autoAllotment() {
        System.out.print("Confirm auto allotment? (yes/no): ");
        if (!"yes".equalsIgnoreCase(sc.nextLine().trim())) { System.out.println("Cancelled."); return; }
        allotmentService.runAutoAllotment(loggedInUser);
    }

    // ── [8] Show All Students ─────────────────────────────────────────────────
    private static void showAllStudents() {
        System.out.println("\n--- All Students ---");
        String sql = "SELECT s.roll_no, s.name, s.branch, s.year, s.gender, " +
                     "s.cgpa, s.attendance, s.status, r.room_number, h.hostel_name " +
                     "FROM STUDENT s " +
                     "LEFT JOIN ALLOTMENT a ON a.roll_no=s.roll_no AND a.status='ACTIVE' " +
                     "LEFT JOIN ROOM r      ON r.room_id=a.room_id " +
                     "LEFT JOIN HOSTEL h    ON h.hostel_id=r.hostel_id " +
                     "ORDER BY s.year, s.branch, s.roll_no";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-16s %-22s %-5s %-4s %-2s %-5s %-6s %-12s %-8s %-18s%n",
                    "RollNo","Name","Br","Yr","G","CGPA","Att%","Status","Room","Hostel");
            System.out.println("-".repeat(105));
            while (rs.next())
                System.out.printf("%-16s %-22s %-5s %-4s %-2s %-5s %-6s %-12s %-8s %-18s%n",
                        rs.getString("roll_no"),      rs.getString("name"),
                        rs.getString("branch"),       rs.getString("year"),
                        rs.getString("gender"),       rs.getBigDecimal("cgpa"),
                        rs.getBigDecimal("attendance"),rs.getString("status"),
                        nvl(rs.getString("room_number"),"-"), nvl(rs.getString("hostel_name"),"-"));
        } catch (SQLException e) { throw new DatabaseException(e.getMessage(), e); }
    }

    // ── [9] Show Rooms ────────────────────────────────────────────────────────
    private static void showAllRooms() {
        System.out.println("\n--- All Rooms ---");
        for (Room r : roomDAO.getAllRooms())
            System.out.printf("ID:%-5d Hostel:%-3d Room:%-8s Type:%-8s Cap:%-3d Occ:%-3d%n",
                    r.getRoomId(), r.getHostelId(), r.getRoomNumber(),
                    r.getRoomType(), r.getCapacity(), r.getOccupied());
    }

    // ── [10] Hostel Status ────────────────────────────────────────────────────
    private static void showHostelStatus() {
        System.out.println("\n--- Hostel Status ---");
        try (PreparedStatement ps = DBConnection.getInstance().getConnection()
                .prepareStatement("SELECT * FROM hostel_status_view ORDER BY hostel_id");
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-4s %-22s %-16s %-15s%n","ID","Hostel","Students","Available");
            System.out.println("-".repeat(60));
            while (rs.next())
                System.out.printf("%-4d %-22s %-16d %-15d%n",
                        rs.getInt("hostel_id"), rs.getString("hostel_name"),
                        rs.getLong("total_students"), rs.getLong("available_rooms"));
        } catch (SQLException e) { throw new DatabaseException(e.getMessage(), e); }
    }

    // ── [11] Search Student Room ──────────────────────────────────────────────
    private static void searchStudentRoom() {
        System.out.println("\n--- Search Student Room ---");
        String roll = prompt("Roll No: ");
        Allotment a = allotmentDAO.getStudentRoom(roll);
        if (a == null) { System.out.println("  No active allotment found."); return; }
        Room   r = roomDAO.getRoomById(a.getRoomId());
        Hostel h = r != null ? hostelDAO.getHostelById(r.getHostelId()) : null;
        System.out.printf("  Roll No  : %s%n", roll);
        System.out.printf("  Room     : %s%n", r != null ? r.getRoomNumber() : "?");
        System.out.printf("  Hostel   : %s%n", h != null ? h.getHostelName() : "?");
        System.out.printf("  Since    : %s%n", a.getAllotmentDate());
    }

    // ── [12] Import XML Manual ────────────────────────────────────────────────
    private static void importXMLManual() {
        System.out.println("\n--- Manual XML Import ---");
        System.out.println("  [1] Single file  [2] Entire folder");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();
        if ("1".equals(c)) {
            int n = xmlService.importFile(prompt("File path: "), loggedInUser);
            System.out.println("  Imported: " + n);
        } else if ("2".equals(c)) {
            int n = xmlService.importFolder(prompt("Folder path: "), loggedInUser);
            System.out.println("  Total: " + n);
        }
    }

    // ── [13] Logs ─────────────────────────────────────────────────────────────
    private static void viewLogs() {
        System.out.println("\n--- Logs (latest 50) ---");
        try (PreparedStatement ps = DBConnection.getInstance().getConnection()
                .prepareStatement("SELECT * FROM LOGS ORDER BY timestamp DESC LIMIT 50");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                System.out.printf("[%s] %-55s (%s)%n",
                        rs.getTimestamp("timestamp"), rs.getString("action"), rs.getString("username"));
        } catch (SQLException e) { throw new DatabaseException(e.getMessage(), e); }
    }

    // ── [14] Day Scholars ─────────────────────────────────────────────────────
    private static void showDayScholars() {
        System.out.println("\n--- Day Scholars ---");
        try (PreparedStatement ps = DBConnection.getInstance().getConnection()
                .prepareStatement("SELECT roll_no,name,branch,year,gender,cgpa,attendance " +
                        "FROM STUDENT WHERE status='DAY_SCHOLAR' ORDER BY year,branch,roll_no");
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-16s %-22s %-5s %-4s %-2s %-5s %-6s%n",
                    "RollNo","Name","Br","Yr","G","CGPA","Att%");
            System.out.println("-".repeat(65));
            int n = 0;
            while (rs.next()) {
                n++;
                System.out.printf("%-16s %-22s %-5s %-4s %-2s %-5s %-6s%n",
                        rs.getString("roll_no"), rs.getString("name"),
                        rs.getString("branch"),  rs.getString("year"),
                        rs.getString("gender"),  rs.getBigDecimal("cgpa"),
                        rs.getBigDecimal("attendance"));
            }
            System.out.println("  Total: " + n);
        } catch (SQLException e) { throw new DatabaseException(e.getMessage(), e); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static void printMenu() {
        System.out.println("""
                
                ╔══════════════════════════════════════════╗
                ║       HOSTEL MANAGEMENT SYSTEM           ║
                ╠══════════════════════════════════════════╣
                ║  [1]  Add Student                        ║
                ║  [2]  Update Student  (by Roll No)       ║
                ║  [3]  Delete Student  (by Roll No)       ║
                ║  [4]  Add Hostel                         ║
                ║  [5]  Add Room                           ║
                ║  [6]  Add Staff                          ║
                ║  [7]  Auto Room Allotment                ║
                ║  [8]  Show All Students                  ║
                ║  [9]  Show All Rooms                     ║
                ║  [10] Hostel Status                      ║
                ║  [11] Search Student Room (by Roll No)   ║
                ║  [12] Import XML (manual)                ║
                ║  [13] View Logs                          ║
                ║  [14] Show Day Scholars                  ║
                ║  [15] Exit                               ║
                ╚══════════════════════════════════════════╝""");
    }

    private static void banner() {
        System.out.println("""
                ╔════════════════════════════════════════╗
                ║    HOSTEL MANAGEMENT SYSTEM            ║
                ║    AIT Pune  |  Java + PostgreSQL      ║
                ╚════════════════════════════════════════╝
                """);
    }

    private static String prompt(String msg) { System.out.print("  " + msg); return sc.nextLine().trim(); }
    private static String promptOr(String msg, String fb) { String v = prompt(msg); return v.isEmpty() ? fb : v; }
    private static int intPrompt(String msg) {
        while (true) { try { return Integer.parseInt(prompt(msg)); } catch (NumberFormatException e) { System.out.println("  Enter a number."); } }
    }
    private static String nvl(String s, String d) { return (s == null || s.isBlank()) ? d : s; }
}
