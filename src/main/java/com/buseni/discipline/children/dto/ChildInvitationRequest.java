package com.buseni.discipline.children.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record ChildInvitationRequest(
    String childId,
    
    @Email(message = "{email.invalid}")
    String email,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "{phone.invalid}")
    String phone
) {} 