package com.buseni.discipline.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "{validation.firstname.required}")
    String firstName,
    
    @NotBlank(message = "{validation.lastname.required}")
    String lastName,
    
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    String email,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "{validation.phone.invalid}")
    String phone,
    
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, message = "{validation.password.size}")
    String password
) {} 