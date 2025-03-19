package com.buseni.discipline.users.service;

import com.buseni.discipline.users.dto.RegisterRequest;
import com.buseni.discipline.users.dto.UserDto;

/**
 * Service for handling authentication operations
 */
public interface AuthService {
    
    /**
     * Register a new user
     * @param request the registration request
     * @return the created user DTO
     */
    UserDto register(RegisterRequest request);
    
    /**
     * Get the current authenticated user
     * @return the user DTO
     */
    UserDto getCurrentUser();
} 