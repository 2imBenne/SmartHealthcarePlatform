package com.smarthealthcareplatform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final com.smarthealthcareplatform.security.JwtAuthenticationFilter jwtAuthFilter;
    private final com.smarthealthcareplatform.security.CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Mở cửa cho Trang chủ, Đăng ký, Đăng nhập, Trang lỗi công cộng
                .requestMatchers("/", "/login", "/register", "/error-page", "/error", "/api/auth/**", "/admin/dashboard").permitAll() 
                
                // CORE-02: KIỂM SOÁT TRUY CẬP CỨNG (Access Control)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/api/patient/**").hasRole("PATIENT")
                
                // Mọi request khác đều phải có JWT Token hợp lệ
                .anyRequest().authenticated()
            )
            // Redirect thông minh khi chưa đăng nhập hoặc không đủ quyền
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    String acceptHeader = request.getHeader("Accept");
                    if (acceptHeader != null && acceptHeader.contains("text/html")) {
                        response.sendRedirect("/error-page?code=401");
                    } else {
                        response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String acceptHeader = request.getHeader("Accept");
                    if (acceptHeader != null && acceptHeader.contains("text/html")) {
                        response.sendRedirect("/error-page?code=403");
                    } else {
                        response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                    }
                })
            )
            // Stateless Session vì dùng JWT
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        
        // CORE-01: Bắt buộc dùng BCrypt băm mật khẩu
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
