package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.entity.*;
import com.smarthealthcareplatform.service.DashboardService;
import com.smarthealthcareplatform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * BUG-05 FIX: Controller chỉ gọi Service, KHÔNG inject Repository trực tiếp.
 * Tuân thủ mô hình 3 lớp: Controller → Service → Repository.
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
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
        User user = dashboardService.getUserByEmail(email);
        UserProfile profile = dashboardService.getProfileByUserId(user.getId());

        model.addAttribute("profile", profile);
        model.addAttribute("appointments", dashboardService.getPatientAppointments(user.getId()));
        model.addAttribute("doctors", dashboardService.getDoctorsAsDTO());
        model.addAttribute("specialties", dashboardService.getAllSpecialties());
        model.addAttribute("history", patientService.getPatientHistory(user.getId()));

        return "patient_dashboard";
    }

    @GetMapping("/doctor/dashboard")
    @PreAuthorize("hasRole('DOCTOR')")
    public String doctorDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = dashboardService.getUserByEmail(email);
        Doctor doctor = dashboardService.getDoctorByUserId(user.getId());

        model.addAttribute("doctor", doctor);
        model.addAttribute("pendingAppointments", dashboardService.getAppointmentsByDoctorAndStatus(doctor.getUserId(), AppointmentStatus.PENDING));
        model.addAttribute("confirmedAppointments", dashboardService.getAppointmentsByDoctorAndStatus(doctor.getUserId(), AppointmentStatus.CONFIRMED));
        model.addAttribute("medicines", dashboardService.getAllMedicines());

        return "doctor_dashboard";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("medicines", dashboardService.getAllMedicines());
        model.addAttribute("prescriptions", dashboardService.getPendingPrescriptions());
        model.addAttribute("users", dashboardService.getAllUsersForAdmin());
        return "admin_dashboard";
    }

    @GetMapping("/error-page")
    public String errorPage(@org.springframework.web.bind.annotation.RequestParam(value = "code", required = false, defaultValue = "404") String code, Model model) {
        model.addAttribute("errorCode", code);
        return "error";
    }
}
