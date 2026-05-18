package com.smarthealthcareplatform.repository;

import com.smarthealthcareplatform.entity.Medicine;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    
    // CORE-08: Sử dụng Pessimistic Write Lock để chặn Race Condition khi nhiều request cùng trừ tồn kho.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<Medicine> findByIdAndIsActiveTrue(Long id);
}
