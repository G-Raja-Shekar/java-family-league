package com.rajashekar.familyleague.notification.repository;

import com.rajashekar.familyleague.notification.entity.EmailLog;
import com.rajashekar.familyleague.notification.entity.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    Page<EmailLog> findByEventType(String eventType, Pageable pageable);

    Page<EmailLog> findByStatus(EmailStatus status, Pageable pageable);
}
