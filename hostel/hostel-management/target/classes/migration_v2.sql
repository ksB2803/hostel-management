-- ============================================================
--  Migration: add gender + day_scholar support
--  Run this AFTER schema.sql if DB already exists,
--  OR add these changes into schema.sql for fresh installs.
-- ============================================================

-- Add gender column to STUDENT
ALTER TABLE STUDENT ADD COLUMN IF NOT EXISTS gender VARCHAR(1) DEFAULT 'M' CHECK (gender IN ('M','F'));

-- Extend status to include DAY_SCHOLAR
ALTER TABLE STUDENT DROP CONSTRAINT IF EXISTS student_status_check;
ALTER TABLE STUDENT ADD CONSTRAINT student_status_check
    CHECK (status IN ('ACTIVE','LEFT','DAY_SCHOLAR'));

-- Extend ALLOTMENT status (no change needed, ACTIVE/LEFT still valid)

-- Hostel reference IDs (adjust if your serial IDs differ)
-- 1 = Visvesvaraya
-- 2 = APJ Abdul Kalam
-- 3 = Sarabai
-- 4 = SN Bose
-- 5 = Kalpana Chawla
-- 6 = Annex
