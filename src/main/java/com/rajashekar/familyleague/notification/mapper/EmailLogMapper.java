package com.rajashekar.familyleague.notification.mapper;

import com.rajashekar.familyleague.notification.dto.EmailLogResponse;
import com.rajashekar.familyleague.notification.entity.EmailLog;
import org.springframework.stereotype.Component;

@Component
public class EmailLogMapper {

    public EmailLogResponse toResponse(EmailLog e) {
        return new EmailLogResponse(
                e.getId(),
                e.getToAddress(),
                e.getSubject(),
                e.getEventType(),
                e.getStatus(),
                e.getSentAt(),
                e.getErrorMessage(),
                e.getCreatedAt()
        );
    }
}
