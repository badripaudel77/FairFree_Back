package com.app.fairfree.repository;

import com.app.fairfree.dto.NotificationResponse;
import com.app.fairfree.model.Notification;
import com.app.fairfree.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserAndReadFalse(User user);
}
