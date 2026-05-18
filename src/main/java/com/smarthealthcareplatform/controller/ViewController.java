package com.smarthealthcareplatform.controller;


import com.smarthealthcareplatform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final com.smarthealthcareplatform.repository.UserRepository userRepository;
    private final com.smarthealthcareplatform.repository.UserProfileRepository userProfileRepository;
    private final com.smarthealthcareplatform.repository.DoctorRepository doctorRepository;
    private final com.smarthealthcareplatform.repository.SpecialtyRepository specialtyRepository;
    private final com.smarthealthcareplatform.repository.AppointmentRepository appointmentRepository;
    private final com.smarthealthcareplatform.repository.MedicineRepository medicineRepository;
    private final com.smarthealthcareplatform.repository.PrescriptionRepository prescriptionRepository;
    private final PatientService patientService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/patient/dashboard")
    @PreAuthorize("hasRole('PATIENT')")
    public String patientDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        com.smarthealthcareplatform.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.smarthealthcareplatform.entity.UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        model.addAttribute("profile", profile);
        model.addAttribute("appointments", appointmentRepository.findByPatientId(user.getId()));
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("specialties", specialtyRepository.findAll());
        model.addAttribute("history", patientService.getPatientHistory(user.getId()));

        return "patient_dashboard";
    }

    @GetMapping("/doctor/dashboard")
    @PreAuthorize("hasRole('DOCTOR')")
    public String doctorDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        com.smarthealthcareplatform.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.smarthealthcareplatform.entity.Doctor doctor = doctorRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        model.addAttribute("doctor", doctor);
        model.addAttribute("pendingAppointments", appointmentRepository.findByDoctorUserIdAndStatus(doctor.getUserId(), com.smarthealthcareplatform.entity.AppointmentStatus.PENDING));
        model.addAttribute("confirmedAppointments", appointmentRepository.findByDoctorUserIdAndStatus(doctor.getUserId(), com.smarthealthcareplatform.entity.AppointmentStatus.CONFIRMED));
        model.addAttribute("medicines", medicineRepository.findAll());

        return "doctor_dashboard";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("medicines", medicineRepository.findAll());
        model.addAttribute("prescriptions", prescriptionRepository.findByStatus(com.smarthealthcareplatform.entity.PrescriptionStatus.PENDING));
        return "admin_dashboard";
    }
}
