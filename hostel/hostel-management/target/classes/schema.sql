-- ============================================================
--  Hostel Management System - PostgreSQL Schema v3
--  roll_no is PRIMARY KEY for STUDENT (no student_id)
-- ============================================================

-- Drop and recreate
DROP TABLE IF EXISTS LOGS       CASCADE;
DROP TABLE IF EXISTS ALLOTMENT  CASCADE;
DROP TABLE IF EXISTS STUDENT    CASCADE;
DROP TABLE IF EXISTS ROOM       CASCADE;
DROP TABLE IF EXISTS STAFF      CASCADE;
DROP TABLE IF EXISTS HOSTEL     CASCADE;
DROP TABLE IF EXISTS USERS      CASCADE;

-- ── USERS ─────────────────────────────────────────────────────────────────────
CREATE TABLE USERS (
    user_id   SERIAL       PRIMARY KEY,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(50)  NOT NULL,
    role      VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','USER'))
);

-- ── STAFF (created before HOSTEL due to circular FK) ──────────────────────────
CREATE TABLE STAFF (
    staff_id   SERIAL       PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(50)  NOT NULL CHECK (role IN ('WARDEN','ATTENDANT','GUARD')),
    hostel_id  INT,
    shift      VARCHAR(20),
    phone      VARCHAR(20)
);

-- ── HOSTEL ────────────────────────────────────────────────────────────────────
CREATE TABLE HOSTEL (
    hostel_id    SERIAL       PRIMARY KEY,
    hostel_name  VARCHAR(100) NOT NULL,
    total_rooms  INT          NOT NULL DEFAULT 0,
    warden_id    INT REFERENCES STAFF(staff_id) ON DELETE SET NULL
);

ALTER TABLE STAFF
    ADD CONSTRAINT fk_staff_hostel
    FOREIGN KEY (hostel_id) REFERENCES HOSTEL(hostel_id) ON DELETE SET NULL;

-- ── ROOM ──────────────────────────────────────────────────────────────────────
CREATE TABLE ROOM (
    room_id      SERIAL      PRIMARY KEY,
    hostel_id    INT         NOT NULL REFERENCES HOSTEL(hostel_id) ON DELETE CASCADE,
    room_number  VARCHAR(10) NOT NULL,
    room_type    VARCHAR(20) NOT NULL CHECK (room_type IN ('SINGLE','DOUBLE','TRIPLE')),
    capacity     INT         NOT NULL CHECK (capacity > 0),
    occupied     INT         NOT NULL DEFAULT 0 CHECK (occupied >= 0),
    CONSTRAINT chk_occupied_capacity CHECK (occupied <= capacity)
);

-- ── STUDENT (roll_no is PK) ───────────────────────────────────────────────────
CREATE TABLE STUDENT (
    roll_no     VARCHAR(50)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    branch      VARCHAR(20),
    year        VARCHAR(5)   CHECK (year IN ('FE','SE','TE','BE')),
    cgpa        DECIMAL(3,2) CHECK (cgpa >= 0 AND cgpa <= 10),
    attendance  DECIMAL(5,2) CHECK (attendance >= 0 AND attendance <= 100),
    email       VARCHAR(100),
    gender      VARCHAR(1)   NOT NULL DEFAULT 'M' CHECK (gender IN ('M','F')),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                             CHECK (status IN ('ACTIVE','LEFT','DAY_SCHOLAR'))
);

-- ── ALLOTMENT (FK → roll_no) ──────────────────────────────────────────────────
CREATE TABLE ALLOTMENT (
    allotment_id    SERIAL       PRIMARY KEY,
    roll_no         VARCHAR(50)  NOT NULL REFERENCES STUDENT(roll_no) ON DELETE CASCADE,
    room_id         INT          NOT NULL REFERENCES ROOM(room_id)    ON DELETE CASCADE,
    allotment_date  DATE         NOT NULL DEFAULT CURRENT_DATE,
    checkout_date   DATE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                                 CHECK (status IN ('ACTIVE','LEFT'))
);

-- ── LOGS ──────────────────────────────────────────────────────────────────────
CREATE TABLE LOGS (
    log_id     SERIAL      PRIMARY KEY,
    action     TEXT        NOT NULL,
    timestamp  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    username   VARCHAR(50)
);

-- ── VIEW ──────────────────────────────────────────────────────────────────────
CREATE OR REPLACE VIEW hostel_status_view AS
SELECT
    h.hostel_id,
    h.hostel_name,
    COUNT(DISTINCT a.roll_no) FILTER (WHERE a.status = 'ACTIVE') AS total_students,
    COALESCE(SUM(r.capacity - r.occupied), 0)                     AS available_rooms
FROM HOSTEL h
LEFT JOIN ROOM      r ON r.hostel_id = h.hostel_id
LEFT JOIN ALLOTMENT a ON a.room_id   = r.room_id
GROUP BY h.hostel_id, h.hostel_name;

-- ── STORED FUNCTION ───────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION calculate_score(p_roll_no VARCHAR)
RETURNS DECIMAL AS $$
DECLARE
    v_cgpa       DECIMAL(3,2);
    v_attendance DECIMAL(5,2);
BEGIN
    SELECT cgpa, attendance
    INTO   v_cgpa, v_attendance
    FROM   STUDENT
    WHERE  roll_no = p_roll_no;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Student % not found', p_roll_no;
    END IF;

    RETURN ((v_attendance / 100.0) * 50) + ((v_cgpa / 10.0) * 50);
END;
$$ LANGUAGE plpgsql;

-- ── SEED DATA ─────────────────────────────────────────────────────────────────
INSERT INTO USERS (username, password, role) VALUES
    ('admin', 'admin123', 'ADMIN'),
    ('user1', 'user123',  'USER');
