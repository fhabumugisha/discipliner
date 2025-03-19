package com.buseni.discipline.users.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.buseni.discipline.users.domain.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
} 