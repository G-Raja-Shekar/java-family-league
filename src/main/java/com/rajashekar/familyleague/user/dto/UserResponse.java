package com.rajashekar.familyleague.user.dto;

import com.rajashekar.familyleague.user.entity.Role;

public record UserResponse(
        Long id,
        String email,
        String displayName,
        String avatarUrl,
        Role role
) {}
