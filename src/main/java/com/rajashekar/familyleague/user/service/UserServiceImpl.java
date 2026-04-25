package com.rajashekar.familyleague.user.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.common.exception.ValidationException;
import com.rajashekar.familyleague.user.dto.RegisterRequest;
import com.rajashekar.familyleague.user.dto.UpdateProfileRequest;
import com.rajashekar.familyleague.user.dto.UserResponse;
import com.rajashekar.familyleague.user.entity.User;
import com.rajashekar.familyleague.user.mapper.UserMapper;
import com.rajashekar.familyleague.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("Email already in use: " + request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getEmail());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return userMapper.toResponse(findByEmail(email));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        if (request.displayName() != null) user.setDisplayName(request.displayName());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        log.info("Profile updated: {}", email);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void softDelete(String email) {
        User user = findByEmail(email);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
        log.info("User soft-deleted: {}", email);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }
}
