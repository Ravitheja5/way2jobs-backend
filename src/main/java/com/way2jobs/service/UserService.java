package com.way2jobs.service;

import com.way2jobs.dto.*;

public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    String resetPassword(ResetPasswordRequest request);

   


    String logout();





    UserResponse getProfile(String token);

UserResponse updateProfile(String token, RegisterRequest request);

String changePassword(String token,
                      String oldPassword,
                      String newPassword);

void deleteAccount(String token);
}