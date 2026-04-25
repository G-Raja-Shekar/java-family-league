package com.rajashekar.familyleague.user.dto;

public record LoginResponse(
        String accessToken,
        long expiresIn
) {}
