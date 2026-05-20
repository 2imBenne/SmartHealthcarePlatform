package com.smarthealthcareplatform.repository;

import com.smarthealthcareplatform.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // CORE-07: Truy vấn phức tạp JOIN FETCH
    @Query("SELECT DISTINCT m FROM MedicalRecord m " +
           "JOIN FETCH m.appointment a " +
           "JOIN FETCH a.doctor d " +
           "JOIN FETCH d.user du " +
           "JOIN FETCH du.profile dup " + // Lấy tên Bác sĩ
           "JOIN FETCH d.specialty s " + // Lấy tên Chuyên khoa
           "LEFT JOIN FETCH m.prescription p " +
           "LEFT JOIN FETCH p.details pd " +
           "LEFT JOIN FETCH pd.medicine med " + // Lấy danh sách Thuốc
           "WHERE a.patient.id = :patientId " +
           "ORDER BY a.appointmentDate DESC")
    List<MedicalRecord> findHistoryByPatientId(@Param("patientId") Long patientId);
}
