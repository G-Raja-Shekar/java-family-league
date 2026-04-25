package com.rajashekar.familyleague.notification.channel;

import com.rajashekar.familyleague.notification.dto.EmailMessage;
import com.rajashekar.familyleague.notification.entity.EmailLog;
import com.rajashekar.familyleague.notification.entity.EmailStatus;
import com.rajashekar.familyleague.notification.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannel {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Override
    public void send(EmailMessage message) {
        EmailLog entry = new EmailLog();
        entry.setToAddress(message.to());
        entry.setSubject(message.subject());
        entry.setBody(message.body());
        entry.setEventType(message.eventType());
        entry.setStatus(EmailStatus.PENDING);
        emailLogRepository.save(entry);

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(message.to());
            mail.setSubject(message.subject());
            mail.setText(message.body());
            mailSender.send(mail);

            entry.setStatus(EmailStatus.SENT);
            entry.setSentAt(Instant.now());
            log.info("Email sent to {}", message.to());
        } catch (MailException ex) {
            entry.setStatus(EmailStatus.FAILED);
            entry.setErrorMessage(ex.getMessage());
            log.warn("Email delivery failed to {}: {}", message.to(), ex.getMessage());
        } finally {
            emailLogRepository.save(entry);
        }
    }
}
