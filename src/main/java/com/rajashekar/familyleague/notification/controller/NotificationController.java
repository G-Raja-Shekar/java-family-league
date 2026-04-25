package com.rajashekar.familyleague.notification.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.response.PagedResponse;
import com.rajashekar.familyleague.notification.dto.BulkEmailRequest;
import com.rajashekar.familyleague.notification.dto.EmailLogResponse;
import com.rajashekar.familyleague.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> sendBulk(@Valid @RequestBody BulkEmailRequest request) {
        notificationService.sendBulk(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PagedResponse<EmailLogResponse>>> getLogs(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                PagedResponse.from(notificationService.getLogs(pageable))));
    }
}
