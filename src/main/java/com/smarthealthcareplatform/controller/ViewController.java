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

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
    private final PatientService patientService;

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean anonymous = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
            if (!anonymous) {
                String email = authentication.getName();
                try {
                    User user = dashboardService.getUserByEmail(email);
                    model.addAttribute("currentUser", user);
                    
                    String fullName = user.getEmail();
                    if (user.getProfile() != null && user.getProfile().getFullName() != null) {
                        fullName = user.getProfile().getFullName();
                    }
                    model.addAttribute("userFullName", fullName);
                    
                    String dashboardUrl = "/patient/dashboard";
                    if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                        dashboardUrl = "/admin/dashboard";
                    } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()))) {
                        dashboardUrl = "/doctor/dashboard";
                    }
                    model.addAttribute("dashboardUrl", dashboardUrl);
                } catch (Exception e) {
                }
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        String redirect = resolveDashboardRedirect(authentication);
        if (redirect != null) {
            return redirect;
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Authentication authentication) {
        String redirect = resolveDashboardRedirect(authentication);
        if (redirect != null) {
            return redirect;
        }
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
        model.addAttribute("pendingPrescriptions", dashboardService.getPendingPrescriptions());

        return "doctor_dashboard";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("medicines", dashboardService.getAllMedicines());
        model.addAttribute("prescriptions", dashboardService.getPendingPrescriptions());
        model.addAttribute("users", dashboardService.getAllUsersForAdmin());
        model.addAttribute("specialties", dashboardService.getAllSpecialties());
        return "admin_dashboard";
    }

    @GetMapping("/error-page")
    public String errorPage(@org.springframework.web.bind.annotation.RequestParam(value = "code", required = false, defaultValue = "404") String code, Model model) {
        model.addAttribute("errorCode", code);
        return "error";
    }

    private String resolveDashboardRedirect(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        boolean anonymous = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
        if (anonymous) {
            return null;
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return "redirect:/admin/dashboard";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()))) {
            return "redirect:/doctor/dashboard";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()))) {
            return "redirect:/patient/dashboard";
        }
        return null;
    }
}
