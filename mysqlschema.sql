CREATE DATABASE IF NOT EXISTS doctor_track_db;
USE doctor_track_db;

DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;

CREATE TABLE doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(60) NOT NULL,
    room_number VARCHAR(30) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    shift_hours VARCHAR(50) NOT NULL DEFAULT '09:00 AM - 05:00 PM',
    consultation_fee DECIMAL(10, 2) NOT NULL DEFAULT 500.00,
    status VARCHAR(30) NOT NULL DEFAULT 'Available'
);

CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    blood_type VARCHAR(10) NOT NULL DEFAULT 'Unknown',
    phone VARCHAR(30) NOT NULL,
    medical_notes VARCHAR(255) DEFAULT ''
);

CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    appointment_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    appointment_time VARCHAR(20) NOT NULL,
    reason VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Seed Sample Doctors
INSERT INTO doctors (full_name, specialization, room_number, phone, shift_hours, consultation_fee, status)
VALUES
    ('Dr. Elena Cruz', 'Cardiology', 'Room 201', '0917-555-0101', '08:00 AM - 04:00 PM', 850.00, 'Available'),
    ('Dr. Marco Reyes', 'Pediatrics', 'Room 105', '0917-555-0102', '09:00 AM - 05:00 PM', 650.00, 'Available'),
    ('Dr. Sophia Lim', 'Dermatology', 'Room 304', '0917-555-0103', '10:00 AM - 06:00 PM', 750.00, 'In Consultation'),
    ('Dr. Rafael Mendoza', 'General Medicine', 'Room 101', '0917-555-0104', '08:00 AM - 05:00 PM', 500.00, 'Available'),
    ('Dr. Hannah Villanueva', 'Orthopedics', 'Room 210', '0917-555-0105', '09:00 AM - 03:00 PM', 900.00, 'On Leave');

-- Seed Sample Patients
INSERT INTO patients (full_name, age, gender, blood_type, phone, medical_notes)
VALUES
    ('Juan Dela Cruz', 42, 'Male', 'O+', '0918-222-3301', 'Mild hypertension, annual ECG monitoring'),
    ('Maria Clara Santos', 8, 'Female', 'A+', '0918-222-3302', 'Routine pediatric wellness & vaccination'),
    ('Carlos Tan', 29, 'Male', 'B+', '0918-222-3303', 'Allergic to penicillin; skin rash consultation'),
    ('Angela Ramos', 35, 'Female', 'AB+', '0918-222-3304', 'Migraine & general physical check-up');

-- Seed Sample Appointments
INSERT INTO appointments (doctor_id, patient_id, appointment_date, appointment_time, reason, status)
VALUES
    (1, 1, CURRENT_DATE, '09:00 AM', 'Blood pressure review & ECG check-up', 'Scheduled'),
    (2, 2, CURRENT_DATE, '10:30 AM', 'Pediatric wellness check-up', 'In Consultation'),
    (3, 3, CURRENT_DATE, '01:30 PM', 'Dermatology skin rash evaluation', 'Scheduled'),
    (4, 4, CURRENT_DATE, '08:30 AM', 'General wellness & migraine follow-up', 'Completed');

SELECT * FROM doctors;
SELECT * FROM patients;
SELECT * FROM appointments;
