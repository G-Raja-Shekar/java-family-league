package com.rajashekar.familyleague.notification.service;

import com.rajashekar.familyleague.common.exception.ResourceNotFoundException;
import com.rajashekar.familyleague.leaderboard.event.LeaderboardRecalculatedEvent;
import com.rajashekar.familyleague.match.entity.Match;
import com.rajashekar.familyleague.match.repository.MatchRepository;
import com.rajashekar.familyleague.notification.channel.NotificationChannel;
import com.rajashekar.familyleague.notification.dto.BulkEmailRequest;
import com.rajashekar.familyleague.notification.dto.EmailLogResponse;
import com.rajashekar.familyleague.notification.dto.EmailMessage;
import com.rajashekar.familyleague.notification.mapper.EmailLogMapper;
import com.rajashekar.familyleague.notification.repository.EmailLogRepository;
import com.rajashekar.familyleague.prediction.match.repository.MatchPredictionRepository;
import com.rajashekar.familyleague.user.entity.User;
import com.rajashekar.familyleague.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationChannel notificationChannel;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailLogMapper emailLogMapper;

    @Value("${app.mail.admin}")
    private String adminEmail;

    @Override
    @Transactional
    public void sendReminder(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));

        Set<Long> predictedUserIds = matchPredictionRepository.findByMatchId(matchId)
                .stream()
                .map(p -> p.getUser().getId())
                .collect(Collectors.toSet());

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!predictedUserIds.contains(user.getId())) {
                notificationChannel.send(new EmailMessage(
                        user.getEmail(),
                        "Reminder: Submit your prediction!",
                        "Don't forget to predict the match starting at " + match.getStartTime(),
                        "MATCH_REMINDER"
                ));
            }
        }
        log.info("Sent reminders for match {}", matchId);
    }

    @EventListener
    public void onLeaderboardRecalculated(LeaderboardRecalculatedEvent event) {
        sendAdminAlert(event.getSeasonId());
    }

    @Override
    public void sendAdminAlert(Long seasonId) {
        notificationChannel.send(new EmailMessage(
                adminEmail,
                "Leaderboard updated for season " + seasonId,
                "The leaderboard has been recalculated for season " + seasonId + ".",
                "LEADERBOARD_UPDATED"
        ));
    }

    @Override
    @Transactional
    public void sendBulk(BulkEmailRequest request) {
        for (Long userId : request.userIds()) {
            userRepository.findById(userId).ifPresent(user ->
                    notificationChannel.send(new EmailMessage(
                            user.getEmail(),
                            request.subject(),
                            request.body(),
                            request.eventType()
                    ))
            );
        }
        log.info("Bulk email sent: eventType={}, recipients={}", request.eventType(), request.userIds().size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getLogs(Pageable pageable) {
        return emailLogRepository.findAll(pageable).map(emailLogMapper::toResponse);
    }
}
