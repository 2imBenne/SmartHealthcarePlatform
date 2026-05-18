package com.smarthealthcareplatform.repository;

import com.smarthealthcareplatform.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
}
