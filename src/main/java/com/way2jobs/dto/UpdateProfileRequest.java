package com.way2jobs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 20, message = "Name must not exceed 20 characters")
    @Pattern(
            regexp = "^[A-Za-z]+(?: [A-Za-z]+)?$",
            message = "Name must contain only English alphabets and at most one space"
    )
    private String name;

    @NotBlank(message = "Gmail is required")
    @Email(message = "Enter a valid Gmail address")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
            message = "Only valid @gmail.com addresses are allowed"
    )
    private String email;

    @NotBlank(message = "Mobile number is required")
    private String mobile;
}