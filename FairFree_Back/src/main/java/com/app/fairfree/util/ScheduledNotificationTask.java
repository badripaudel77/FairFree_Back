package com.app.fairfree.util;

import com.app.fairfree.model.Item;
import com.app.fairfree.service.ItemService;
import com.app.fairfree.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;


@Component
public class ScheduledNotificationTask {

    private final NotificationService notificationService;

    private final ItemService itemService;

    public ScheduledNotificationTask(NotificationService notificationService, ItemService itemService) {
        this.notificationService = notificationService;
        this.itemService = itemService;
    }

    @Scheduled(cron = "0 13 20 * * *")
    public void checkExpiringItems() {
        List<Item> items = itemService.getExpiringItems();
        for (Item item : items) {
                LocalDateTime expiration = item.getCreatedAt().plusDays(item.getExpiresAfterDays());
                long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), expiration);
                String message = item.getName() + " will expire with in " + daysLeft + " days.";
                notificationService.pushNotification(item.getOwner(), message);

                String subject = "Expiring Items Alert";
                notificationService.sendEmailNotification(item.getOwner().getEmail(), subject, message);
        }
    }


}

