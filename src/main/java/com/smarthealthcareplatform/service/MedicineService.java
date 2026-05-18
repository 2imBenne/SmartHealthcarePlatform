package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.MedicineRequest;
import com.smarthealthcareplatform.entity.Medicine;
import com.smarthealthcareplatform.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {
    private final MedicineRepository medicineRepository;

    // Xem: Lấy tất cả danh mục thuốc
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    // Xem: Lấy 1 loại thuốc theo ID
    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc có ID: " + id));
    }

    // Thêm: Tạo thuốc mới
    @Transactional
    public Medicine createMedicine(MedicineRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .unit(request.getUnit())
                .stockQuantity(request.getStockQuantity())
                .price(request.getPrice())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return medicineRepository.save(medicine);
    }

    // Sửa: Cập nhật thông tin thuốc
    @Transactional
    public Medicine updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = getMedicineById(id);
        
        medicine.setName(request.getName());
        medicine.setUnit(request.getUnit());
        medicine.setStockQuantity(request.getStockQuantity());
        medicine.setPrice(request.getPrice());
        if (request.getIsActive() != null) {
            medicine.setIsActive(request.getIsActive());
        }

        return medicineRepository.save(medicine);
    }

    // Xóa (Xóa mềm - Soft Delete): Tránh vỡ liên kết bảng Đơn thuốc cũ
    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = getMedicineById(id);
        medicine.setIsActive(false); // Chuyển trạng thái sang Ngừng bán
        medicineRepository.save(medicine);
    }
}
