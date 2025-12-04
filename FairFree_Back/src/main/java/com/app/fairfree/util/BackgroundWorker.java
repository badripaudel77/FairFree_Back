package com.app.fairfree.util;

import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.Item;
import com.app.fairfree.repository.ItemRepository;
import com.app.fairfree.service.ItemService;
import com.app.fairfree.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Component
public class BackgroundWorker extends Thread {

    @Autowired
    NotificationService notificationService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemService itemService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron = "0 10 23 * * *")
    public void checkExpiringItems() {
        List<Item> items = itemService.getExpiringItems();
        List<Item> expiredItems = new ArrayList<>();
        for (Item item : items) {
                LocalDateTime expiration = item.getCreatedAt().plusDays(item.getExpiresAfterDays());
                long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), expiration);
                if (daysLeft <= 0) {
                    item.setStatus(ItemStatus.EXPIRED);
                    expiredItems.add(item);
                    continue; // Skip notification for already expired items
                }
                String message = item.getTitle() + " will expire with in " + daysLeft + " days.";
                notificationService.pushNotification(item.getOwner(), message);

                String subject = "Expiring Items Alert";
                notificationService.sendEmailNotification(item.getOwner().getEmail(), subject, message);
                logger.info("Expiration notification to {} sent at {}", item.getOwner().getFullName(), item.getOwner().getEmail());
        }
        if (!expiredItems.isEmpty()) {
            itemRepository.saveAll(expiredItems);
            logger.info("Marked {} items as expired.", expiredItems.size());
        }
    }

}

