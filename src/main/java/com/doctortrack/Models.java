package com.doctortrack;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Models {

    private Models() {
    }

    public static class DoctorItem {
        private final int id;
        private final String fullName;
        private final String specialization;
        private final String roomNumber;
        private final String phone;
        private final String shiftHours;
        private final BigDecimal consultationFee;
        private final String status;

        public DoctorItem(int id, String fullName, String specialization, String roomNumber,
                          String phone, String shiftHours, BigDecimal consultationFee, String status) {
            this.id = id;
            this.fullName = fullName;
            this.specialization = specialization;
            this.roomNumber = roomNumber;
            this.phone = phone;
            this.shiftHours = shiftHours;
            this.consultationFee = consultationFee;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public String getSpecialization() {
            return specialization;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public String getPhone() {
            return phone;
        }

        public String getShiftHours() {
            return shiftHours;
        }

        public BigDecimal getConsultationFee() {
            return consultationFee;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return fullName + " (" + specialization + " • " + status + ")";
        }
    }

    public static class PatientItem {
        private final int id;
        private final String fullName;
        private final int age;
        private final String gender;
        private final String bloodType;
        private final String phone;
        private final String medicalNotes;

        public PatientItem(int id, String fullName, int age, String gender,
                           String bloodType, String phone, String medicalNotes) {
            this.id = id;
            this.fullName = fullName;
            this.age = age;
            this.gender = gender;
            this.bloodType = bloodType;
            this.phone = phone;
            this.medicalNotes = medicalNotes;
        }

        public int getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public int getAge() {
            return age;
        }

        public String getGender() {
            return gender;
        }

        public String getBloodType() {
            return bloodType;
        }

        public String getPhone() {
            return phone;
        }

        public String getMedicalNotes() {
            return medicalNotes;
        }

        @Override
        public String toString() {
            return fullName + " (" + age + "y/" + gender + " • " + bloodType + ")";
        }
    }

    public static class AppointmentItem {
        private final int id;
        private final int doctorId;
        private final String doctorName;
        private final String specialization;
        private final int patientId;
        private final String patientName;
        private final LocalDate appointmentDate;
        private final String appointmentTime;
        private final String reason;
        private final String status;

        public AppointmentItem(int id, int doctorId, String doctorName, String specialization,
                               int patientId, String patientName, LocalDate appointmentDate,
                               String appointmentTime, String reason, String status) {
            this.id = id;
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.specialization = specialization;
            this.patientId = patientId;
            this.patientName = patientName;
            this.appointmentDate = appointmentDate;
            this.appointmentTime = appointmentTime;
            this.reason = reason;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public int getDoctorId() {
            return doctorId;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getSpecialization() {
            return specialization;
        }

        public int getPatientId() {
            return patientId;
        }

        public String getPatientName() {
            return patientName;
        }

        public LocalDate getAppointmentDate() {
            return appointmentDate;
        }

        public String getAppointmentTime() {
            return appointmentTime;
        }

        public String getReason() {
            return reason;
        }

        public String getStatus() {
            return status;
        }
    }
}
