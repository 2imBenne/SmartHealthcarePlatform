package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.MedicineRequest;
import com.smarthealthcareplatform.entity.Medicine;
import com.smarthealthcareplatform.service.MedicalService;
import com.smarthealthcareplatform.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/medicines")
@RequiredArgsConstructor
// Bảo vệ toàn bộ class này, chỉ Admin mới được phép gọi API (CORE-02 & CORE-04)
@PreAuthorize("hasRole('ADMIN')") 
public class MedicineController {

    private final MedicineService medicineService;
    private final MedicalService medicalService;

    // Thêm / Tạo mới
    @PostMapping
    public ResponseEntity<Medicine> createMedicine(@RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.createMedicine(request));
    }

    // Xem danh sách
    @GetMapping
    public ResponseEntity<List<Medicine>> getAllMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    // Xem chi tiết 1 thuốc
    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    // Sửa
    @PutMapping("/{id}")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long id, @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, request));
    }

    // Xóa (Xóa mềm)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok("Đã chuyển thuốc vào trạng thái Ngừng Bán (Xóa mềm).");
    }

    // CORE-08: Admin / Dược sĩ xác nhận phát thuốc
    @PostMapping("/prescriptions/{prescriptionId}/dispense")
    public ResponseEntity<String> dispensePrescription(@PathVariable Long prescriptionId) {
        medicalService.dispensePrescription(prescriptionId);
        return ResponseEntity.ok("Cấp phát thuốc thành công, đã trừ tồn kho (Pessimistic Lock applied).");
    }
}
