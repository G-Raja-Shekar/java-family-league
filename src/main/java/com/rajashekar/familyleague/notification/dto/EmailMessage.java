package com.rajashekar.familyleague.notification.dto;

public record EmailMessage(String to, String subject, String body, String eventType) {}
