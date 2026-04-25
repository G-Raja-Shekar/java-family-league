package com.rajashekar.familyleague.notification.scheduler;

import com.rajashekar.familyleague.match.repository.MatchRepository;
import com.rajashekar.familyleague.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final MatchRepository matchRepository;

    @Scheduled(cron = "${scheduler.match-reminder.cron}")
    public void sendMatchReminders() {
        Instant windowStart = Instant.now();
        Instant windowEnd = windowStart.plus(1, ChronoUnit.HOURS);

        matchRepository.findByStartTimeBetween(windowStart, windowEnd).forEach(match -> {
            try {
                notificationService.sendReminder(match.getId());
            } catch (Exception ex) {
                log.warn("Reminder failed for match {}: {}", match.getId(), ex.getMessage());
            }
        });
    }
}
