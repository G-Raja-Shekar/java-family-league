package com.rajashekar.familyleague.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkEmailRequest(
        @NotEmpty List<Long> userIds,
        @NotBlank String eventType,
        @NotBlank String subject,
        @NotBlank String body
) {}
