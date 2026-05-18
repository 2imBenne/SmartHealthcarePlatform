package com.smarthealthcareplatform.repository;

import com.smarthealthcareplatform.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;

import com.smarthealthcareplatform.entity.AppointmentStatus;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorUserId(Long doctorId);
    List<Appointment> findByDoctorUserIdAndStatus(Long doctorId, AppointmentStatus status);
}
