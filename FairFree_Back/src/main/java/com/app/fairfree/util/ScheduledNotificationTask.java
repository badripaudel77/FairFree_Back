package com.app.fairfree.util;

import com.app.fairfree.service.NotificationService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;


@Component
public class ScheduledNotificationTask {

    private final NotificationService notificationService;

    public ScheduledNotificationTask(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 1 8 * * *") // 8:01 AM daily
    public void sendDailyNotifications() {

        String subject = "Daily Report - " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String message = "Good morning! This is your daily notification.";

//        for (String recipient : recipients) {
//            try {
                notificationService.sendNotification("davefsaha@gmail.com", subject, message);
//            } catch (Exception e) {
//                log.error("Failed to send notification to {}: {}", recipient, e.getMessage());
//            }
        }


}

