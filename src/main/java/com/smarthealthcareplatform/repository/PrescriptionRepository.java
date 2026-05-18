package com.smarthealthcareplatform.repository;

import com.smarthealthcareplatform.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smarthealthcareplatform.entity.PrescriptionStatus;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByStatus(PrescriptionStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p FROM Prescription p " +
           "LEFT JOIN FETCH p.medicalRecord mr " +
           "LEFT JOIN FETCH mr.appointment app " +
           "LEFT JOIN FETCH app.patient pat " +
           "LEFT JOIN FETCH pat.profile prof " +
           "LEFT JOIN FETCH p.details d " +
           "LEFT JOIN FETCH d.medicine m " +
           "WHERE p.status = :status")
    List<Prescription> findByStatusWithAllDetails(@org.springframework.data.repository.query.Param("status") PrescriptionStatus status);
}
