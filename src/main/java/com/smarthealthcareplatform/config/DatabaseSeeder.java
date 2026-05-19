package com.smarthealthcareplatform.config;

import com.smarthealthcareplatform.entity.*;
import com.smarthealthcareplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final SpecialtyRepository specialtyRepository;
    private final TestTypeRepository testTypeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DoctorRepository doctorRepository;
    private final MedicineRepository medicineRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Tự động Seed Chuyên Khoa
        if (specialtyRepository.count() == 0) {
            List<Specialty> specialties = Arrays.asList(
                    Specialty.builder().name("Nội tổng quát").description("Khám và điều trị các bệnh lý nội khoa chung.").build(),
                    Specialty.builder().name("Ngoại tổng quát").description("Khám, chẩn đoán và điều trị phẫu thuật.").build(),
                    Specialty.builder().name("Nhi khoa").description("Chăm sóc sức khỏe cho trẻ em và trẻ sơ sinh.").build(),
                    Specialty.builder().name("Sản phụ khoa").description("Chăm sóc sức khỏe sinh sản cho phụ nữ.").build(),
                    Specialty.builder().name("Tim mạch").description("Chuyên sâu về các bệnh lý tim và mạch máu.").build(),
                    Specialty.builder().name("Da liễu").description("Khám và điều trị các bệnh về da, tóc, móng.").build(),
                    Specialty.builder().name("Tai Mũi Họng").description("Điều trị các bệnh lý liên quan đến tai, mũi, họng.").build(),
                    Specialty.builder().name("Răng Hàm Mặt").description("Chăm sóc sức khỏe răng miệng.").build()
            );
            specialtyRepository.saveAll(specialties);
            System.out.println("✅ Đã tự động nạp dữ liệu nền cho danh mục Chuyên khoa!");
        }

        // 2. Tự động Seed Users & UserProfiles nếu hệ thống trống trơn
        // 1.1 Seed loai xet nghiem lam danh muc nen (CORE-04)
        if (testTypeRepository.count() == 0) {
            List<TestType> testTypes = Arrays.asList(
                    TestType.builder().name("Cong thuc mau").description("Danh gia hong cau, bach cau, tieu cau.").build(),
                    TestType.builder().name("Sinh hoa mau").description("Danh gia duong huyet, men gan, chuc nang than.").build(),
                    TestType.builder().name("Nuoc tieu").description("Sang loc nhiem khuan tiet nieu, than va chuyen hoa.").build(),
                    TestType.builder().name("X-Quang").description("Hinh anh chan doan co xuong, phoi, tim.").build(),
                    TestType.builder().name("Sieu am").description("Khao sat mo mem, o bung, tim mach va san khoa.").build(),
                    TestType.builder().name("Dien tim (ECG)").description("Theo doi hoat dong dien hoc cua tim.").build()
            );
            testTypeRepository.saveAll(testTypes);
            System.out.println("Seeded test_types successfully.");
        }

        if (userRepository.count() == 0) {
            String defaultPassword = passwordEncoder.encode("password123");

            // --- ADMIN ---
            User admin = User.builder()
                    .email("admin@healthcare.com")
                    .passwordHash(defaultPassword)
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            admin = userRepository.save(admin);

            UserProfile adminProfile = UserProfile.builder()
                    .user(admin)
                    .fullName("Hệ thống Quản trị")
                    .phoneNumber("0901234567")
                    .gender("Nam")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .address("TP. Hồ Chí Minh")
                    .build();
            userProfileRepository.save(adminProfile);

            // --- PATIENTS ---
            User patient1 = User.builder()
                    .email("lamgiahuy1308@gmail.com")
                    .passwordHash(defaultPassword)
                    .role(Role.PATIENT)
                    .isActive(true)
                    .build();
            patient1 = userRepository.save(patient1);

            UserProfile profile1 = UserProfile.builder()
                    .user(patient1)
                    .fullName("Lâm Gia Huy")
                    .phoneNumber("0783579771")
                    .gender("Nam")
                    .dateOfBirth(LocalDate.of(2002, 8, 15))
                    .address("TP. Hồ Chí Minh")
                    .build();
            userProfileRepository.save(profile1);

            User patient2 = User.builder()
                    .email("patient@healthcare.com")
                    .passwordHash(defaultPassword)
                    .role(Role.PATIENT)
                    .isActive(true)
                    .build();
            patient2 = userRepository.save(patient2);

            UserProfile profile2 = UserProfile.builder()
                    .user(patient2)
                    .fullName("Nguyễn Thu Thảo")
                    .phoneNumber("0987654321")
                    .gender("Nữ")
                    .dateOfBirth(LocalDate.of(1998, 5, 20))
                    .address("Hà Nội")
                    .build();
            userProfileRepository.save(profile2);

            // --- DOCTORS ---
            User docUser1 = User.builder()
                    .email("doctor.hung@healthcare.com")
                    .passwordHash(defaultPassword)
                    .role(Role.DOCTOR)
                    .isActive(true)
                    .build();
            docUser1 = userRepository.save(docUser1);

            UserProfile docProfile1 = UserProfile.builder()
                    .user(docUser1)
                    .fullName("BS. Nguyễn Văn Hùng")
                    .phoneNumber("0912345678")
                    .gender("Nam")
                    .dateOfBirth(LocalDate.of(1978, 12, 10))
                    .address("Chợ Rẫy, TP. Hồ Chí Minh")
                    .build();
            userProfileRepository.save(docProfile1);

            User docUser2 = User.builder()
                    .email("doctor.mai@healthcare.com")
                    .passwordHash(defaultPassword)
                    .role(Role.DOCTOR)
                    .isActive(true)
                    .build();
            docUser2 = userRepository.save(docUser2);

            UserProfile docProfile2 = UserProfile.builder()
                    .user(docUser2)
                    .fullName("BS. Trần Thị Mai")
                    .phoneNumber("0923456789")
                    .gender("Nữ")
                    .dateOfBirth(LocalDate.of(1985, 3, 24))
                    .address("Cầu Giấy, Hà Nội")
                    .build();
            userProfileRepository.save(docProfile2);

            User docUser3 = User.builder()
                    .email("doctor.vy@healthcare.com")
                    .passwordHash(defaultPassword)
                    .role(Role.DOCTOR)
                    .isActive(true)
                    .build();
            docUser3 = userRepository.save(docUser3);

            UserProfile docProfile3 = UserProfile.builder()
                    .user(docUser3)
                    .fullName("BS. Phạm Thảo Vy")
                    .phoneNumber("0934567890")
                    .gender("Nữ")
                    .dateOfBirth(LocalDate.of(1982, 7, 18))
                    .address("Quận 1, TP. Hồ Chí Minh")
                    .build();
            userProfileRepository.save(docProfile3);

            System.out.println("✅ Đã tự động nạp dữ liệu nền cho Users & UserProfiles!");

            // 3. Tự động Seed Doctors (Tim mạch, Nhi khoa, Sản phụ khoa)
            Specialty timMach = specialtyRepository.findAll().stream()
                    .filter(s -> s.getName().equals("Tim mạch")).findFirst().orElse(null);
            Specialty nhiKhoa = specialtyRepository.findAll().stream()
                    .filter(s -> s.getName().equals("Nhi khoa")).findFirst().orElse(null);
            Specialty sanPhu = specialtyRepository.findAll().stream()
                    .filter(s -> s.getName().equals("Sản phụ khoa")).findFirst().orElse(null);

            Doctor doctor1 = Doctor.builder()
                    .user(docUser1)
                    .specialty(timMach)
                    .experienceYears(15)
                    .qualifications("Thạc sĩ, Bác sĩ chuyên khoa Tim mạch - ĐH Y Dược")
                    .consultationFee(BigDecimal.valueOf(200000))
                    .build();
            doctor1 = doctorRepository.save(doctor1);

            Doctor doctor2 = Doctor.builder()
                    .user(docUser2)
                    .specialty(nhiKhoa)
                    .experienceYears(8)
                    .qualifications("Bác sĩ Cử nhân Nhi khoa - Đại học Y Hà Nội")
                    .consultationFee(BigDecimal.valueOf(150000))
                    .build();
            doctor2 = doctorRepository.save(doctor2);

            Doctor doctor3 = Doctor.builder()
                    .user(docUser3)
                    .specialty(sanPhu)
                    .experienceYears(12)
                    .qualifications("Bác sĩ Chuyên khoa II Sản Phụ khoa - BV Từ Dũ")
                    .consultationFee(BigDecimal.valueOf(180000))
                    .build();
            doctor3 = doctorRepository.save(doctor3);

            System.out.println("✅ Đã tự động nạp dữ liệu nền cho Doctors!");

            // 4. Tự động Seed Medicines
            List<Medicine> medicines = Arrays.asList(
                    Medicine.builder().name("Paracetamol 500mg").unit("Viên").stockQuantity(500).price(BigDecimal.valueOf(1500)).isActive(true).build(),
                    Medicine.builder().name("Ibuprofen 400mg").unit("Viên").stockQuantity(300).price(BigDecimal.valueOf(2500)).isActive(true).build(),
                    Medicine.builder().name("Amoxicillin 500mg").unit("Viên").stockQuantity(200).price(BigDecimal.valueOf(3500)).isActive(true).build(),
                    Medicine.builder().name("Vitamin C 500mg").unit("Viên").stockQuantity(1000).price(BigDecimal.valueOf(1000)).isActive(true).build(),
                    Medicine.builder().name("Cetirizine 10mg").unit("Viên").stockQuantity(400).price(BigDecimal.valueOf(1800)).isActive(true).build(),
                    Medicine.builder().name("Gaviscon Dual Action").unit("Gói").stockQuantity(150).price(BigDecimal.valueOf(9500)).isActive(true).build(),
                    Medicine.builder().name("Panadol Extra").unit("Viên").stockQuantity(800).price(BigDecimal.valueOf(2000)).isActive(true).build()
            );
            medicines = medicineRepository.saveAll(medicines);
            System.out.println("✅ Đã tự động nạp dữ liệu nền cho Medicines!");

            // 5. Tự động Seed Appointments
            Appointment app1 = Appointment.builder()
                    .patient(patient1)
                    .doctor(doctor1)
                    .appointmentDate(LocalDate.now().minusDays(3))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(9, 30))
                    .status(AppointmentStatus.COMPLETED)
                    .reason("Khám định kỳ huyết áp cao")
                    .build();
            app1 = appointmentRepository.save(app1);

            Appointment app2 = Appointment.builder()
                    .patient(patient1)
                    .doctor(doctor2)
                    .appointmentDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 30))
                    .status(AppointmentStatus.CONFIRMED)
                    .reason("Tư vấn dinh dưỡng cho trẻ")
                    .build();
            appointmentRepository.save(app2);

            Appointment app3 = Appointment.builder()
                    .patient(patient1)
                    .doctor(doctor3)
                    .appointmentDate(LocalDate.now().plusDays(2))
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(14, 30))
                    .status(AppointmentStatus.PENDING)
                    .reason("Khám phụ khoa định kỳ")
                    .build();
            appointmentRepository.save(app3);

            Appointment app4 = Appointment.builder()
                    .patient(patient2)
                    .doctor(doctor1)
                    .appointmentDate(LocalDate.now().minusDays(5))
                    .startTime(LocalTime.of(8, 30))
                    .endTime(LocalTime.of(9, 0))
                    .status(AppointmentStatus.COMPLETED)
                    .reason("Nhói ngực nhẹ khi vận động mạnh")
                    .build();
            app4 = appointmentRepository.save(app4);

            System.out.println("✅ Đã tự động nạp dữ liệu nền cho Appointments!");

            // 6. Tự động Seed MedicalRecords & Prescriptions
            MedicalRecord record1 = MedicalRecord.builder()
                    .appointment(app1)
                    .symptoms("Đau đầu nhẹ, huyết áp đo được 140/90 mmHg")
                    .diagnosis("Tăng huyết áp vô căn độ 1")
                    .advice("Uống thuốc đúng giờ, hạn chế ăn mặn, tập thể dục nhẹ nhàng.")
                    .build();
            record1 = medicalRecordRepository.save(record1);

            Prescription pres1 = Prescription.builder()
                    .medicalRecord(record1)
                    .status(PrescriptionStatus.PENDING)
                    .build();
            pres1 = prescriptionRepository.save(pres1);

            PrescriptionDetail detail1_1 = PrescriptionDetail.builder()
                    .prescription(pres1)
                    .medicine(medicines.get(0)) // Paracetamol
                    .quantity(10)
                    .dosageInstructions("Uống 1 viên khi đau đầu, tối đa 3 lần/ngày.")
                    .unitPrice(medicines.get(0).getPrice())
                    .build();
            prescriptionDetailRepository.save(detail1_1);

            PrescriptionDetail detail1_2 = PrescriptionDetail.builder()
                    .prescription(pres1)
                    .medicine(medicines.get(3)) // Vitamin C
                    .quantity(20)
                    .dosageInstructions("Uống 1 viên mỗi sáng sau ăn.")
                    .unitPrice(medicines.get(3).getPrice())
                    .build();
            prescriptionDetailRepository.save(detail1_2);


            MedicalRecord record2 = MedicalRecord.builder()
                    .appointment(app4)
                    .symptoms("Khó thở nhẹ khi leo cầu thang, tim đập nhanh")
                    .diagnosis("Thiếu máu cơ tim nhẹ")
                    .advice("Tránh stress, ăn nhiều rau xanh, tái khám sau 2 tuần.")
                    .build();
            record2 = medicalRecordRepository.save(record2);

            Prescription pres2 = Prescription.builder()
                    .medicalRecord(record2)
                    .status(PrescriptionStatus.DISPENSED)
                    .build();
            pres2 = prescriptionRepository.save(pres2);

            PrescriptionDetail detail2_1 = PrescriptionDetail.builder()
                    .prescription(pres2)
                    .medicine(medicines.get(1)) // Ibuprofen
                    .quantity(15)
                    .dosageInstructions("Uống 1 viên sau ăn no sáng/tối.")
                    .unitPrice(medicines.get(1).getPrice())
                    .build();
            prescriptionDetailRepository.save(detail2_1);

            PrescriptionDetail detail2_2 = PrescriptionDetail.builder()
                    .prescription(pres2)
                    .medicine(medicines.get(6)) // Panadol Extra
                    .quantity(10)
                    .dosageInstructions("Uống 1 viên khi nhức đầu.")
                    .unitPrice(medicines.get(6).getPrice())
                    .build();
            prescriptionDetailRepository.save(detail2_2);

            System.out.println("✅ Đã tự động nạp dữ liệu nền cho MedicalRecords & Prescriptions!");
        }
    }
}
