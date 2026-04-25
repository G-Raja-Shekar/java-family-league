package com.rajashekar.familyleague.notification.service;

import com.rajashekar.familyleague.notification.dto.BulkEmailRequest;
import com.rajashekar.familyleague.notification.dto.EmailLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void sendReminder(Long matchId);

    void sendAdminAlert(Long seasonId);

    void sendBulk(BulkEmailRequest request);

    Page<EmailLogResponse> getLogs(Pageable pageable);
}
