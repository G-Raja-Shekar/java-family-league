package com.rajashekar.familyleague.notification.dto;

import com.rajashekar.familyleague.notification.entity.EmailStatus;

import java.time.Instant;

public record EmailLogResponse(
        Long id,
        String toAddress,
        String subject,
        String eventType,
        EmailStatus status,
        Instant sentAt,
        String errorMessage,
        Instant createdAt
) {}
