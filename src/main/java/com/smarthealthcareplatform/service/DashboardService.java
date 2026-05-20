package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.entity.*;
import com.smarthealthcareplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientService patientService;

    // ======================== PATIENT DASHBOARD ========================

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + email));
    }

    @Transactional(readOnly = true)
    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ cá nhân"));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDoctorsAsDTO() {
        return doctorRepository.findAll().stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", doc.getUserId());

            String docName = "Bác sĩ";
            if (doc.getUser() != null && doc.getUser().getProfile() != null) {
                docName = doc.getUser().getProfile().getFullName();
            }

            Map<String, Object> specialtyMap = new HashMap<>();
            if (doc.getSpecialty() != null) {
                specialtyMap.put("id", doc.getSpecialty().getId());
                specialtyMap.put("name", doc.getSpecialty().getName());
            }

            Map<String, Object> userMap = new HashMap<>();
            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put("fullName", docName);
            userMap.put("profile", profileMap);

            map.put("specialty", specialtyMap);
            map.put("user", userMap);
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    // ======================== DOCTOR DASHBOARD ========================

    @Transactional(readOnly = true)
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctorAndStatus(Long doctorId, AppointmentStatus status) {
        return appointmentRepository.findByDoctorUserIdAndStatus(doctorId, status);
    }

    @Transactional(readOnly = true)
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    // ======================== ADMIN DASHBOARD ========================

    @Transactional(readOnly = true)
    public List<Prescription> getPendingPrescriptions() {
        return prescriptionRepository.findByStatusWithAllDetails(PrescriptionStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllUsersForAdmin() {
        return userRepository.findAll().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("email", user.getEmail());
            map.put("role", user.getRole().name());
            map.put("isActive", user.getIsActive());
            map.put("createdAt", user.getCreatedAt());

            UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
            map.put("fullName", profile != null ? profile.getFullName() : "N/A");
            map.put("phoneNumber", profile != null ? profile.getPhoneNumber() : "N/A");
            map.put("gender", profile != null ? profile.getGender() : "N/A");
            return map;
        }).collect(Collectors.toList());
    }
}
