-- ============================================================
-- AI-Powered Campus Issue Resolution System
-- MySQL Database Schema
-- ============================================================
-- Run this script ONCE before starting the application.
-- (Or let Spring Boot create tables automatically via ddl-auto=update)
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_resolver_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_resolver_db;

-- ──────────────────────────────────────────────
-- Table: students
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS students (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100)    NOT NULL,
    email               VARCHAR(150)    NOT NULL UNIQUE,
    roll_number         VARCHAR(20)     NOT NULL UNIQUE,
    student_department  VARCHAR(100),
    phone_number        VARCHAR(15),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_student_email       (email),
    INDEX idx_student_roll_number (roll_number)
);

-- ──────────────────────────────────────────────
-- Table: departments
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100)    NOT NULL UNIQUE,
    description         TEXT,
    contact_email       VARCHAR(150),
    head_name           VARCHAR(100),
    category_handled    VARCHAR(50)     NOT NULL UNIQUE,

    INDEX idx_dept_category (category_handled)
);

-- ──────────────────────────────────────────────
-- Table: complaints
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS complaints (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT            NOT NULL,

    -- AI Classification Fields
    ai_category         VARCHAR(50),
    priority            ENUM('URGENT','NORMAL') DEFAULT 'NORMAL',
    ai_reasoning        TEXT,

    -- Complaint Status
    status              ENUM('PENDING','IN_PROGRESS','RESOLVED','CLOSED','REJECTED')
                        NOT NULL DEFAULT 'PENDING',
    admin_notes         TEXT,

    -- Timestamps
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at         DATETIME,

    -- Foreign Keys
    student_id          BIGINT          NOT NULL,
    department_id       BIGINT,

    CONSTRAINT fk_complaint_student    FOREIGN KEY (student_id)    REFERENCES students(id),
    CONSTRAINT fk_complaint_department FOREIGN KEY (department_id) REFERENCES departments(id),

    INDEX idx_complaint_status     (status),
    INDEX idx_complaint_priority   (priority),
    INDEX idx_complaint_category   (ai_category),
    INDEX idx_complaint_student    (student_id),
    INDEX idx_complaint_department (department_id)
);

-- ──────────────────────────────────────────────
-- Seed: Default Departments
-- ──────────────────────────────────────────────
INSERT IGNORE INTO departments (name, description, contact_email, head_name, category_handled) VALUES
    ('IT Department',         'Handles WiFi and network issues',               'it@college.edu',          'Dr. Arun Kumar',   'wifi'),
    ('Hostel Office',         'Manages hostel facilities and residential',      'hostel@college.edu',      'Mr. Suresh Patel', 'hostel'),
    ('Transport Office',      'Handles bus and campus transport issues',        'transport@college.edu',   'Ms. Priya Nair',   'transport'),
    ('Maintenance Department','Handles electrical, plumbing, and repairs',      'maintenance@college.edu', 'Mr. Rajesh Singh', 'maintenance'),
    ('Admin Office',          'Handles general and uncategorized complaints',   'admin@college.edu',       'Dr. Meera Iyer',   'other');

-- ──────────────────────────────────────────────
-- Seed: Sample Student (for testing)
-- ──────────────────────────────────────────────
INSERT IGNORE INTO students (name, email, roll_number, student_department, phone_number) VALUES
    ('Rahul Sharma',   'rahul@college.edu',   'CS21B001', 'Computer Science', '9876543210'),
    ('Priya Mehta',    'priya@college.edu',   'EE21B045', 'Electrical Engineering', '9876543211'),
    ('Arjun Reddy',    'arjun@college.edu',   'ME21B012', 'Mechanical Engineering', '9876543212');

-- ──────────────────────────────────────────────
-- Verification Query
-- ──────────────────────────────────────────────
SELECT 'Schema setup complete!' AS status;
SELECT COUNT(*) AS department_count FROM departments;
SELECT COUNT(*) AS student_count FROM students;
