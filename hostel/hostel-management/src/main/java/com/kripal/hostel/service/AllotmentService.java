package com.kripal.hostel.service;

import com.kripal.hostel.dao.AllotmentDAO;
import com.kripal.hostel.dao.RoomDAO;
import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.exception.RoomFullException;
import com.kripal.hostel.model.Room;
import com.kripal.hostel.model.Student;
import com.kripal.hostel.util.DBConnection;
import com.kripal.hostel.util.LogUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * =====================================================================
 *  HOSTEL ALLOTMENT RULES (with overflow)
 * =====================================================================
 *  GIRLS (all years)  → Kalpana Chawla         overflow → DAY_SCHOLAR
 *  FE  boys           → APJ Abdul Kalam        overflow → SE pool (Sarabai/Vis C/D)
 *  SE  IT  boys       → Annex first            overflow → SE pool
 *  SE  all boys       → Sarabai + Vis C/D      overflow → APJ leftover
 *  TE  boys           → Visvesvaraya A/B        overflow → SN Bose
 *  BE  boys           → SN Bose                overflow → Visvesvaraya A/B leftover
 *
 *  Within each hostel group: PROPORTIONAL by class size, top scorers get rooms.
 * =====================================================================
 */
public class AllotmentService {

    private final RoomDAO      roomDAO      = new RoomDAO();
    private final AllotmentDAO allotmentDAO = new AllotmentDAO();

    private static final int H_VISVESVARAYA   = 1;
    private static final int H_APJ            = 2;
    private static final int H_SARABAI        = 3;
    private static final int H_SNBOSE         = 4;
    private static final int H_KALPANA_CHAWLA = 5;
    private static final int H_ANNEX          = 6;

    public void runAutoAllotment(String username) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║    AUTOMATIC PROPORTIONAL ALLOTMENT      ║");
        System.out.println("╚══════════════════════════════════════════╝");

        List<Student> all = getUnallotted();
        if (all.isEmpty()) { System.out.println("  No pending students."); return; }
        System.out.printf("  Pending: %d students%n%n", all.size());

        List<ScoredStudent> scored = scoreAll(all);
        List<ScoredStudent> girls  = filter(scored, null, null, true);
        List<ScoredStudent> boys   = filter(scored, null, null, false);
        int ds = 0;

        // ── GIRLS → Kalpana Chawla ────────────────────────────────────────────
        System.out.println("── GIRLS → Kalpana Chawla ──────────────────────────");
        ds += allotProportional(girls, getRooms(H_KALPANA_CHAWLA, null), username, true);

        // ── FE BOYS → APJ (overflow → SE pool) ───────────────────────────────
        System.out.println("\n── FE Boys → APJ Abdul Kalam ───────────────────────");
        List<ScoredStudent> feBoys = filter(boys, "FE", null, false);
        List<ScoredStudent> feOverflow = allotWithOverflow(feBoys, getRooms(H_APJ, null), username);
        System.out.printf("  FE overflow to SE pool: %d students%n", feOverflow.size());

        // ── SE IT BOYS → Annex (overflow → SE pool) ───────────────────────────
        System.out.println("\n── SE IT Boys → Annex ──────────────────────────────");
        List<ScoredStudent> seItBoys = filter(boys, "SE", "IT", false);
        List<ScoredStudent> seItOverflow = allotWithOverflow(seItBoys, getRooms(H_ANNEX, null), username);

        // ── SE ALL BOYS → Sarabai + Vis C/D + APJ overflow ───────────────────
        System.out.println("\n── SE Boys → Sarabai + Visvesvaraya C/D ────────────");
        List<ScoredStudent> seNonItBoys = filter(boys, "SE", null, false)
                .stream().filter(s -> !"IT".equals(s.student.getBranch())).collect(Collectors.toList());
        List<ScoredStudent> sePool = new ArrayList<>(seNonItBoys);
        sePool.addAll(seItOverflow);
        sePool.addAll(feOverflow);  // FE overflow joins SE pool
        sePool.sort(Comparator.comparing(s -> s.score, Comparator.reverseOrder()));

        List<Room> seRooms = new ArrayList<>(getRooms(H_SARABAI, null));
        seRooms.addAll(getRooms(H_VISVESVARAYA, List.of("C","D")));
        List<ScoredStudent> seOverflow = allotWithOverflow(sePool, seRooms, username);
        System.out.printf("  SE overflow: %d students%n", seOverflow.size());

        // ── TE BOYS → Vis A/B (overflow → SN Bose) ───────────────────────────
        System.out.println("\n── TE Boys → Visvesvaraya A/B ──────────────────────");
        List<ScoredStudent> teBoys = filter(boys, "TE", null, false);
        List<Room> visAB = getRooms(H_VISVESVARAYA, List.of("A","B"));
        List<ScoredStudent> teOverflow = allotWithOverflow(teBoys, visAB, username);
        System.out.printf("  TE overflow to SN Bose: %d students%n", teOverflow.size());

        // ── BE BOYS → SN Bose (overflow → Vis A/B leftover) ──────────────────
        System.out.println("\n── BE Boys → SN Bose ───────────────────────────────");
        List<ScoredStudent> beBoys = filter(boys, "BE", null, false);
        List<ScoredStudent> bePool = new ArrayList<>(beBoys);
        bePool.addAll(teOverflow);  // TE overflow joins BE/SN Bose pool
        bePool.sort(Comparator.comparing(s -> s.score, Comparator.reverseOrder()));
        List<ScoredStudent> beOverflow = allotWithOverflow(bePool, getRooms(H_SNBOSE, null), username);

        // BE overflow → Vis A/B leftover
        if (!beOverflow.isEmpty()) {
            System.out.println("\n── BE/TE Overflow → Visvesvaraya A/B leftover ──────");
            List<Room> visABLeft = getRooms(H_VISVESVARAYA, List.of("A","B"));
            beOverflow = allotWithOverflow(beOverflow, visABLeft, username);
        }

        // ── Final day scholars ────────────────────────────────────────────────
        System.out.println("\n── Remaining Overflow → Day Scholars ───────────────");
        for (ScoredStudent ss : beOverflow) {
            markDayScholar(ss.student.getRollNo());
            System.out.printf("  [DAY SCHOLAR] %s%n", ss.student.getName());
            ds++;
        }
        for (ScoredStudent ss : seOverflow) {
            markDayScholar(ss.student.getRollNo());
            System.out.printf("  [DAY SCHOLAR] %s%n", ss.student.getName());
            ds++;
        }

        System.out.printf("\n══ Allotment complete. Day scholars: %d%n", ds);
        LogUtil.log("Auto-allotment complete. Day scholars: " + ds, username);
    }

    // ── Proportional allotment — returns day scholar count ───────────────────
    private int allotProportional(List<ScoredStudent> students, List<Room> rooms,
                                   String username, boolean markDs) {
        List<ScoredStudent> overflow = allotWithOverflow(students, rooms, username);
        if (markDs) {
            for (ScoredStudent ss : overflow) {
                markDayScholar(ss.student.getRollNo());
                System.out.printf("  [DAY SCHOLAR] %-25s %.2f%n", ss.student.getName(), ss.score);
            }
        }
        return overflow.size();
    }

    /** Allots proportionally, returns list of students who couldn't get rooms. */
    private List<ScoredStudent> allotWithOverflow(List<ScoredStudent> students,
                                                   List<Room> rooms, String username) {
        if (students.isEmpty()) { System.out.println("  (none)"); return List.of(); }

        int totalSeats    = totalSeats(rooms);
        int totalStudents = students.size();

        // Group by class
        Map<String, List<ScoredStudent>> byClass = new LinkedHashMap<>();
        for (ScoredStudent ss : students)
            byClass.computeIfAbsent(classKey(ss.student), k -> new ArrayList<>()).add(ss);

        // Proportional quota
        Map<String, Integer> quota = new LinkedHashMap<>();
        int used = 0;
        for (var e : byClass.entrySet()) {
            int q = (int) Math.floor((double) e.getValue().size() / totalStudents * totalSeats);
            quota.put(e.getKey(), q);
            used += q;
        }
        int leftover = totalSeats - used;

        // Print quota table
        System.out.printf("  %-18s %5s %5s%n", "Class", "Size", "Quota");
        System.out.println("  " + "-".repeat(30));
        for (var e : byClass.entrySet())
            System.out.printf("  %-18s %5d %5d%n", e.getKey(), e.getValue().size(), quota.get(e.getKey()));
        System.out.printf("  Seats available: %d  Leftover: %d%n%n", totalSeats, leftover);

        RoomQueue rq = new RoomQueue(rooms, roomDAO);
        List<ScoredStudent> notAllotted = new ArrayList<>();

        // Allot within quota
        for (var e : byClass.entrySet()) {
            int q = quota.get(e.getKey()), given = 0;
            for (ScoredStudent ss : e.getValue()) {
                if (given < q) {
                    Room r = rq.next();
                    if (r != null && assign(ss, r, username)) given++;
                    else notAllotted.add(ss);
                } else notAllotted.add(ss);
            }
        }

        // Distribute leftover to top scorers
        if (leftover > 0 && !notAllotted.isEmpty()) {
            notAllotted.sort(Comparator.comparing(s -> s.score, Comparator.reverseOrder()));
            List<ScoredStudent> remaining = new ArrayList<>();
            for (ScoredStudent ss : notAllotted) {
                if (leftover > 0) {
                    Room r = rq.next();
                    if (r != null && assign(ss, r, username)) { leftover--; continue; }
                }
                remaining.add(ss);
            }
            notAllotted = remaining;
        }

        return notAllotted;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public BigDecimal calculateScore(Student s) {
        try (CallableStatement cs = DBConnection.getInstance().getConnection()
                .prepareCall("{ ? = call calculate_score(?) }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setString(2, s.getRollNo());
            cs.execute();
            return cs.getBigDecimal(1);
        } catch (SQLException e) {
            double score = (s.getAttendance().doubleValue() / 100.0 * 50)
                         + (s.getCgpa().doubleValue()        / 10.0  * 50);
            return BigDecimal.valueOf(Math.round(score * 100.0) / 100.0);
        }
    }

    private List<ScoredStudent> scoreAll(List<Student> students) {
        List<ScoredStudent> list = new ArrayList<>();
        for (Student s : students) list.add(new ScoredStudent(s, calculateScore(s)));
        list.sort(Comparator.comparing(ss -> ss.score, Comparator.reverseOrder()));
        return list;
    }

    private List<ScoredStudent> filter(List<ScoredStudent> all, String year, String branch, boolean girls) {
        return all.stream()
                .filter(ss -> ss.student.isGirl() == girls)
                .filter(ss -> year   == null || year.equals(ss.student.getYear()))
                .filter(ss -> branch == null || branch.equals(ss.student.getBranch()))
                .collect(Collectors.toList());
    }

    private boolean assign(ScoredStudent ss, Room room, String username) {
        try {
            allotmentDAO.assignRoom(ss.student.getRollNo(), room.getRoomId());
            System.out.printf("  [OK] %-28s %.2f → %s%n",
                    ss.student.getName(), ss.score, room.getRoomNumber());
            LogUtil.log("Allotted: " + ss.student.getRollNo() + " → room " + room.getRoomId(), username);
            return true;
        } catch (RoomFullException e) { return false; }
    }

    private void markDayScholar(String rollNo) {
        try (PreparedStatement ps = DBConnection.getInstance().getConnection()
                .prepareStatement("UPDATE STUDENT SET status='DAY_SCHOLAR' WHERE roll_no=?")) {
            ps.setString(1, rollNo);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("markDayScholar: " + e.getMessage(), e); }
    }

    private String classKey(Student s) {
        if (s.getRollNo() != null) {
            String[] p = s.getRollNo().split("-");
            if (p.length >= 3) return p[0] + "-" + p[1] + "-" + p[2];
            if (p.length == 2) return p[0] + "-" + p[1];
        }
        return s.getYear() + "-" + s.getBranch();
    }

    private List<Room> getRooms(int hostelId, List<String> flanks) {
        String sql = (flanks == null || flanks.isEmpty())
                ? "SELECT * FROM ROOM WHERE hostel_id=? AND occupied < capacity ORDER BY room_number"
                : "SELECT * FROM ROOM WHERE hostel_id=? AND occupied < capacity AND (" +
                  flanks.stream().map(f -> "room_number LIKE '" + f + "%'")
                        .collect(Collectors.joining(" OR ")) + ") ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, hostelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rooms.add(new Room(rs.getInt("room_id"), rs.getInt("hostel_id"),
                        rs.getString("room_number"), rs.getString("room_type"),
                        rs.getInt("capacity"), rs.getInt("occupied")));
            }
        } catch (SQLException e) { throw new DatabaseException("getRooms: " + e.getMessage(), e); }
        return rooms;
    }

    private int totalSeats(List<Room> rooms) {
        return rooms.stream().mapToInt(r -> r.getCapacity() - r.getOccupied()).sum();
    }

    private List<Student> getUnallotted() {
        String sql = "SELECT * FROM STUDENT WHERE status='ACTIVE' AND roll_no NOT IN " +
                     "(SELECT roll_no FROM ALLOTMENT WHERE status='ACTIVE')";
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Student(
                    rs.getString("roll_no"), rs.getString("name"), rs.getString("branch"),
                    rs.getString("year"), rs.getBigDecimal("cgpa"), rs.getBigDecimal("attendance"),
                    rs.getString("email"), rs.getString("gender"), rs.getString("status")));
        } catch (SQLException e) { throw new DatabaseException("getUnallotted: " + e.getMessage(), e); }
        return list;
    }

    private static class ScoredStudent {
        final Student student; final BigDecimal score;
        ScoredStudent(Student s, BigDecimal sc) { student = s; score = sc; }
    }

    private static class RoomQueue {
        private final LinkedList<Room> q;
        private final RoomDAO dao;
        RoomQueue(List<Room> rooms, RoomDAO dao) { q = new LinkedList<>(rooms); this.dao = dao; }
        Room next() {
            while (!q.isEmpty()) {
                Room fresh = dao.getRoomById(q.peek().getRoomId());
                if (fresh != null && fresh.hasSpace()) { q.set(0, fresh); return fresh; }
                q.poll();
            }
            return null;
        }
    }
}
