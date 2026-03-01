# 🏨 Hostel Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?style=for-the-badge&logo=postgresql)
![JDBC](https://img.shields.io/badge/JDBC-Advanced-green?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**A production-grade console application for managing hostel allotments at AIT Pune.**  
Built with Java 17 + PostgreSQL using advanced JDBC — no ORM, no shortcuts.

[Features](#-features) • [Algorithm](#-allotment-algorithm) • [Setup](#-getting-started) • [Structure](#-project-structure) • [Screenshots](#-screenshots)

</div>

---

## 📌 About the Project

The **Hostel Management System** automates the entire hostel room allotment process for **Army Institute of Technology, Pune**. It manages **1,280 students** across **6 hostels** using a merit-based proportional allotment algorithm that fairly distributes rooms by CGPA and attendance.

This project demonstrates every advanced JDBC concept expected in top placement interviews — transactions, stored procedures, batch inserts, row locking, views, and clean layered architecture.

---

## ✨ Features

| Module | What it does |
|---|---|
| 🧑‍🎓 **Student Management** | Add / Update / Delete by Roll No. Auto-import 1,280 students from 32 XML files on startup |
| 🏠 **Hostel & Rooms** | 6 hostels, 740 rooms with flank/floor naming, live occupancy tracking |
| 🎯 **Smart Allotment** | Merit-based proportional algorithm — girls → Kalpana Chawla, year-wise routing for boys |
| 📊 **Reports** | All students with room (4-table JOIN), hostel status (DB view), day scholars list |
| 📁 **XML Import** | DOM parser + batch insert of entire `student_data/` folder automatically |
| 🔐 **Auth & Audit** | Login with roles (ADMIN/USER), every action logged with timestamp |

---

## 🔬 Advanced JDBC Concepts Used

```
✅ PreparedStatement      — Every single DB call. Zero raw Statement usage.
✅ JDBC Transactions      — setAutoCommit(false) → FOR UPDATE → INSERT+UPDATE → commit/rollback
✅ Row-Level Locking      — SELECT ... FOR UPDATE prevents double-booking during allotment
✅ CallableStatement      — Calls PostgreSQL calculate_score(roll_no) stored function
✅ Batch Processing       — addBatch() / executeBatch() for bulk XML student import
✅ RETURN_GENERATED_KEYS  — Captures auto-generated IDs after every INSERT
✅ Singleton Pattern      — DBConnection.java — one shared connection instance
✅ Database View          — hostel_status_view for live occupancy reporting
✅ Stored Function        — calculate_score() written in PL/pgSQL
✅ DOM Parser             — javax.xml.parsers reads student XML files
✅ Custom Exceptions      — DatabaseException, RoomFullException
```

---

## 📁 Project Structure

```
hostel-management/
│
├── 📁 src/main/java/com/kripal/hostel/
│   │
│   ├── 📄 Main.java                       # 15-option console menu
│   │
│   ├── 📁 model/                          # Pure POJOs — no logic
│   │   ├── Student.java                   # roll_no is PRIMARY KEY
│   │   ├── Hostel.java
│   │   ├── Room.java
│   │   ├── Staff.java
│   │   ├── Allotment.java
│   │   └── User.java
│   │
│   ├── 📁 dao/                            # DB layer — PreparedStatement only
│   │   ├── StudentDAO.java                # Full CRUD by roll_no
│   │   ├── HostelDAO.java
│   │   ├── RoomDAO.java
│   │   ├── StaffDAO.java
│   │   ├── AllotmentDAO.java              # ⭐ JDBC Transactions + Row Lock
│   │   └── UserDAO.java
│   │
│   ├── 📁 service/                        # Business logic
│   │   ├── AllotmentService.java          # ⭐ Algorithm + CallableStatement
│   │   ├── XMLImportService.java          # ⭐ DOM Parser + Batch Processing
│   │   └── LoginService.java
│   │
│   ├── 📁 util/
│   │   ├── DBConnection.java              # ⭐ Singleton connection
│   │   └── LogUtil.java                   # Audit logging
│   │
│   └── 📁 exception/
│       ├── DatabaseException.java
│       └── RoomFullException.java
│
├── 📁 src/main/resources/
│   └── schema.sql                         # Tables + View + Stored Function
│
├── 📁 student_data/                       # 32 XML files — auto-imported on startup
│   ├── FE_IT_A.xml  FE_IT_B.xml
│   ├── SE_COMP_A.xml  SE_COMP_B.xml
│   └── ... (32 files, 1280 students)
│
├── 📄 hostel_seed_v2.sql                  # Hostels, rooms, staff seed data
└── 📄 pom.xml
```

---

## 🗄️ Database Design

```
STUDENT (roll_no PK) ──────── ALLOTMENT ──────── ROOM ──────── HOSTEL
                                                               │
                                                             STAFF (warden_id)

VIEW:     hostel_status_view     →  live students + available rooms per hostel
FUNCTION: calculate_score(roll_no) →  merit = (att/100×50) + (cgpa/10×50)
```

### Hostel Map

| # | Hostel | Rooms | For |
|---|--------|-------|-----|
| 1 | Visvesvaraya | 224 | TE boys (A/B flank) · SE overflow (C/D flank) |
| 2 | APJ Abdul Kalam | 160 | FE boys |
| 3 | Sarabai | 60 | SE non-IT boys (ground floor) |
| 4 | SN Bose | 160 | BE boys |
| 5 | Kalpana Chawla | 96 | All girls (any year) |
| 6 | Annex | 40 | SE IT boys only (all TRIPLE rooms) |

---

## 🎯 Allotment Algorithm

```
All students scored:  score = (attendance/100 × 50) + (cgpa/10 × 50)

GIRLS  ──────────────────────────────→  Kalpana Chawla
                          overflow  →  DAY SCHOLAR

FE boys  ────────────────────────────→  APJ Abdul Kalam
                          overflow  →  joins SE pool ↓

SE IT boys  ─────────────────────────→  Annex
                          overflow  →  joins SE pool ↓

SE boys + overflow pool  ────────────→  Sarabai + Visvesvaraya C/D
                          overflow  →  DAY SCHOLAR

TE boys  ────────────────────────────→  Visvesvaraya A/B
                          overflow  →  joins BE/SN Bose pool ↓

BE boys + TE overflow  ──────────────→  SN Bose
                          overflow  →  Visvesvaraya A/B leftover
                          overflow  →  DAY SCHOLAR

Within each hostel group:
  1. Group students by class  (FE-IT-A, SE-COMP-B, TE-MECH …)
  2. Quota = classSize / groupTotal × availableSeats   (proportional)
  3. Within quota → top merit scorers get rooms first
  4. Leftover seats → highest scorers across all classes
```

---

## 📸 Screenshots

### 🚀 Startup
*App launches, connects to DB and shows the system banner*

![Startup](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/01_startup.png)

### 🔐 Login
*Secure login with role-based access — ADMIN / USER*

![Login](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/02_login.png)

### 📋 Main Menu
*15-option console menu — full hostel management at your fingertips*

![Menu](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/03_menu.png)

### ➕ Add Student
*Add a new student manually — roll number is the unique identifier*

![Add Student](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/02_add_student.png)

### 📊 Hostel Status
*Live occupancy pulled from `hostel_status_view` — students and available rooms per hostel*

![Hostel Status](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/04_hostel_status.png)

### 🎯 Auto Allotment — Proportional Quota
*Shows class-wise proportional quota table before assigning rooms by merit score*

![Allotment Quota](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/05_allotment_quota.png)

### 🎯 Auto Allotment — Room Assignment
*Each student assigned a room based on merit score within their class quota*

![Allotment Detail](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/05b_allotment_quota.png)

### 👥 All Students
*JOIN across STUDENT → ALLOTMENT → ROOM → HOSTEL — full student-room view*

![All Students](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/06_students.png)

### 🔍 Search Student Room
*Enter roll number → instantly returns room number, hostel and allotment date*

![Search Room](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/07_search.png)

### 🎓 Day Scholars
*Students beyond hostel capacity automatically marked as Day Scholar*

![Day Scholars](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/08_day_scholars.png)

![Day Scholars Continued](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/08b_day_scholars.png)

### 📜 Audit Logs
*Every action logged with timestamp, action details and username*

![Logs](https://raw.githubusercontent.com/ksB2803/hostel-management/main/hostel/hostel-management/docs/screenshots/09_logs.png)

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 15+
- IntelliJ IDEA
- Maven (built into IntelliJ)

### Step 1 — Clone the Repo
```bash
git clone https://github.com/ksB2803/hostel-management.git
cd hostel-management
```

### Step 2 — Create Database
In pgAdmin, open Query Tool and run:
```sql
CREATE DATABASE hostel_db;
```

### Step 3 — Run SQL Scripts
Connect to `hostel_db` and run in this order:
```
1. src/main/resources/schema.sql     ← tables, view, stored function
2. hostel_seed_v2.sql                ← hostels, 740 rooms, staff
```

### Step 4 — Configure Connection
Edit `src/main/java/com/kripal/hostel/util/DBConnection.java`:
```java
private static final String DB_URL      = "jdbc:postgresql://localhost:5432/hostel_db";
private static final String DB_USER     = "postgres";
private static final String DB_PASSWORD = "YOUR_PASSWORD";
```

### Step 5 — Add Student Data
Place all 32 XML files into `student_data/` at the project root:
```
hostel-management/
├── student_data/
│   ├── FE_IT_A.xml
│   ├── FE_IT_B.xml
│   └── ... (all 32 files)
└── src/
```

### Step 6 — Run
Open `Main.java` in IntelliJ and click **▶ Run**.

---

## 🔑 Default Credentials

| Username | Password | Role |
|----------|----------|------|
| `admin`  | `admin123` | ADMIN |
| `user1`  | `user123`  | USER |

---

## 📄 XML Student Format

```xml
<students>
    <student>
        <n>Vikram Reddy</n>
        <roll_no>FE-IT-A-001</roll_no>
        <branch>IT</branch>
        <year>FE</year>
        <cgpa>8.10</cgpa>
        <attendance>90.67</attendance>
        <email>vikram.reddy@aitpune.edu.in</email>
        <gender>M</gender>
    </student>
</students>
```

**Roll number format:** `YEAR-BRANCH-SECTION-NUMBER`

| Example | Meaning |
|---------|---------|
| `FE-IT-A-001` | First Year · IT · Section A · Student 1 |
| `SE-COMP-B-023` | Second Year · COMP · Section B · Student 23 |
| `TE-MECH-015` | Third Year · MECH (no section) · Student 15 |

---

## 🛠️ Tech Stack

| Tech | Version | Purpose |
|------|---------|---------|
| Java | 17 | Core language |
| PostgreSQL | 15+ | Database |
| JDBC | — | DB connectivity (intentionally no ORM) |
| Maven | 3.8+ | Build + dependency management |
| PL/pgSQL | — | Stored function for merit score |
| DOM Parser | built-in | XML student file parsing |

---

## 📜 License

MIT License — free to use for learning.

---

<div align="center">

Made with ☕ by <a href="https://github.com/ksB2803">ksB2803</a> · AIT Pune


</div>
