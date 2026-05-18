package com.smarthealthcareplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", uniqueConstraints = {
    // CORE-05: Database level constraint against double-booking a doctor
    @UniqueConstraint(columnNames = {"doctor_id", "appointment_date", "start_time"}) 
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private com.smarthealthcareplatform.entity.User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private com.smarthealthcareplatform.entity.Doctor doctor;

    @Column(nullable = false, name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(nullable = false, name = "start_time")
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.smarthealthcareplatform.entity.AppointmentStatus status;

    @Column(length = 500)
    private String reason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
