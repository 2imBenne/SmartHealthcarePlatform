package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.MedicineRequest;
import com.smarthealthcareplatform.dto.MedicineResponse;
import com.smarthealthcareplatform.entity.Medicine;
import com.smarthealthcareplatform.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {
    private final MedicineRepository medicineRepository;

    // Xem: Lấy tất cả danh mục thuốc
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Xem: Lấy 1 loại thuốc theo ID
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc có ID: " + id));
        return mapToResponse(medicine);
    }

    // Thêm: Tạo thuốc mới
    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .unit(request.getUnit())
                .stockQuantity(request.getStockQuantity())
                .price(request.getPrice())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return mapToResponse(medicineRepository.save(medicine));
    }

    // Sửa: Cập nhật thông tin thuốc
    @Transactional
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc có ID: " + id));
        
        medicine.setName(request.getName());
        medicine.setUnit(request.getUnit());
        medicine.setStockQuantity(request.getStockQuantity());
        medicine.setPrice(request.getPrice());
        if (request.getIsActive() != null) {
            medicine.setIsActive(request.getIsActive());
        }

        return mapToResponse(medicineRepository.save(medicine));
    }

    // Xóa (Xóa mềm - Soft Delete): Tránh vỡ liên kết bảng Đơn thuốc cũ
    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc có ID: " + id));
        medicine.setIsActive(false); // Chuyển trạng thái sang Ngừng bán
        medicineRepository.save(medicine);
    }

    // BUG-06 FIX: Mapper Entity → DTO (không phơi bày Entity ra ngoài)
    private MedicineResponse mapToResponse(Medicine m) {
        return MedicineResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .unit(m.getUnit())
                .stockQuantity(m.getStockQuantity())
                .price(m.getPrice())
                .isActive(m.getIsActive())
                .build();
    }
}
