package com.way2jobs.service.impl;

import com.way2jobs.dto.ChangePasswordRequest;
import com.way2jobs.dto.ForgotPasswordRequest;
import com.way2jobs.dto.LoginRequest;
import com.way2jobs.dto.LoginResponse;
import com.way2jobs.dto.RegisterRequest;
import com.way2jobs.dto.ResetPasswordRequest;
import com.way2jobs.dto.UpdateProfileRequest;
import com.way2jobs.dto.UserResponse;
import com.way2jobs.entity.User;
import com.way2jobs.repository.UserRepository;
import com.way2jobs.security.JwtUtil;
import com.way2jobs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .build();

        User saved = userRepository.save(user);

        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .mobile(saved.getMobile())
                .role(saved.getRole())
                .enabled(saved.getEnabled())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Login Successful")
                .build();
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        return "Password reset feature will be implemented with Email OTP.";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return "Password Reset Successfully";
    }

    @Override
    public UserResponse getProfile(String token) {

        User user = userRepository.findByEmail(jwtUtil.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .build();
    }

    @Override
    public UserResponse updateProfile(String token, UpdateProfileRequest request) {

        String currentEmail = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getMobile() != null) {
            if (!java.util.Objects.equals(user.getMobile(), request.getMobile())
                    && userRepository.existsByMobile(request.getMobile())) {
                throw new RuntimeException("Mobile already exists");
            }

            user.setMobile(request.getMobile());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        User updated = userRepository.save(user);

        return UserResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .email(updated.getEmail())
                .mobile(updated.getMobile())
                .role(updated.getRole())
                .enabled(updated.getEnabled())
                .build();
    }

    @Override
    public String changePassword(String token,
                                 String oldPassword,
                                 String newPassword) {

        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return "Password changed successfully.";
    }

    @Override
    public String logout() {

        return "Logout Successful";
    }

    @Override
    public void deleteAccount(String token) {

        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
}