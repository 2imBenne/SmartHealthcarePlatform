package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.AuthResponse;
import com.smarthealthcareplatform.dto.LoginRequest;
import com.smarthealthcareplatform.dto.RegisterRequest;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.entity.UserProfile;
import com.smarthealthcareplatform.repository.UserProfileRepository;
import com.smarthealthcareplatform.repository.UserRepository;
import com.smarthealthcareplatform.security.CustomUserDetailsService;
import com.smarthealthcareplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    // CORE-01 & CORE-03: Đăng ký + Tạo Hồ sơ
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // BUG-01 FIX: Chặn đăng ký với role ADMIN từ API public
        if (request.getRole() == null || request.getRole() == com.smarthealthcareplatform.entity.Role.ADMIN) {
            throw new RuntimeException("Không được phép đăng ký với vai trò Quản trị viên (ADMIN)!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        if (request.getPhoneNumber() != null && userProfileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng!");
        }

        // Băm mật khẩu và lưu User
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) 
                .role(request.getRole())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // Khởi tạo Hồ sơ cá nhân (CORE-03)
        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();
        userProfileRepository.save(profile);

        // Trả về Token để đăng nhập ngay luôn
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        
        return new AuthResponse(jwtToken, "Đăng ký thành công!", "ROLE_" + user.getRole().name());
    }

    // CORE-01: Đăng nhập
    public AuthResponse login(LoginRequest request) {
        // Spring Security sẽ tự động kiểm tra password hash có khớp hay không
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        return new AuthResponse(jwtToken, "Đăng nhập thành công!", "ROLE_" + user.getRole().name());
    }
}
