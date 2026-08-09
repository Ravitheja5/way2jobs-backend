package com.way2jobs.controller;

import com.way2jobs.dto.ChangePasswordRequest;
import com.way2jobs.dto.UpdateProfileRequest;
import com.way2jobs.dto.UserResponse;
import com.way2jobs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(userService.getProfile(token));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(userService.updateProfile(token, request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(userService.changePassword(token, request.getOldPassword(), request.getNewPassword()));
    }

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(
            @RequestHeader("Authorization") String token) {

        userService.deleteAccount(token);

        return ResponseEntity.ok("Account Deleted Successfully");
    }
}
