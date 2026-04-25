package com.rajashekar.familyleague.user.dto;

public record UpdateProfileRequest(
        String displayName,
        String avatarUrl
) {}
