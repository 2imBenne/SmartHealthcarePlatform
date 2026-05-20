package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.AuthResponse;
import com.smarthealthcareplatform.dto.LoginRequest;
import com.smarthealthcareplatform.dto.RegisterRequest;
import com.smarthealthcareplatform.entity.Role;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.entity.UserProfile;
import com.smarthealthcareplatform.repository.UserProfileRepository;
import com.smarthealthcareplatform.repository.UserRepository;
import com.smarthealthcareplatform.security.CustomUserDetailsService;
import com.smarthealthcareplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.security.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.login.lock-minutes:15}")
    private int lockMinutes;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == null || request.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không được phép đăng ký với vai trò ADMIN");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        if (request.getPhoneNumber() != null && userProfileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }

        boolean isDoctor = request.getRole() == Role.DOCTOR;
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(!isDoctor) // DOCTOR requires admin approval, PATIENT is active immediately
                .failedLoginAttempts(0)
                .accountLockedUntil(null)
                .build();
        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();
        userProfileRepository.save(profile);

        if (isDoctor) {
            return new AuthResponse(null, "Đăng ký thành công! Vui lòng chờ Admin phê duyệt tài khoản bác sĩ trước khi đăng nhập.", "ROLE_" + user.getRole().name());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken, "Đăng ký thành công", "ROLE_" + user.getRole().name());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (isLocked(user)) {
            throw new LockedException("Tài khoản tạm khóa đến " + user.getAccountLockedUntil());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            boolean locked = handleFailedLogin(user);
            if (locked && user != null && user.getAccountLockedUntil() != null) {
                throw new LockedException("Tài khoản tạm khóa đến " + user.getAccountLockedUntil());
            }
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        User existing = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
        clearFailedLogin(existing);
        return new AuthResponse(jwtToken, "Đăng nhập thành công", "ROLE_" + existing.getRole().name());
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            jwtService.revokeToken(token);
        }
    }

    private boolean isLocked(User user) {
        return user != null
                && user.getAccountLockedUntil() != null
                && user.getAccountLockedUntil().isAfter(LocalDateTime.now());
    }

    private boolean handleFailedLogin(User user) {
        if (user == null) {
            return false;
        }
        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        attempts++;
        boolean locked = false;
        if (attempts >= maxFailedAttempts) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
            locked = true;
        } else {
            user.setFailedLoginAttempts(attempts);
        }
        userRepository.save(user);
        return locked;
    }

    private void clearFailedLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);
    }
}
