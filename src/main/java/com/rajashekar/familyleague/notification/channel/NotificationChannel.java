package com.rajashekar.familyleague.notification.channel;

import com.rajashekar.familyleague.notification.dto.EmailMessage;

public interface NotificationChannel {

    void send(EmailMessage message);
}
