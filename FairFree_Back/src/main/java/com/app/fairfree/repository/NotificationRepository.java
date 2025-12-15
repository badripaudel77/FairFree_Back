package com.app.fairfree.repository;

import com.app.fairfree.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = """
            SELECT *
            FROM notifications n
            WHERE n.user_id = :userId
              AND n.created_at >= COALESCE(:howLongAgo, '1970-01-01'::timestamp)
            ORDER BY n.read ASC, n.created_at DESC
            """, nativeQuery = true)
    List<Notification> getAllNotificationsForTheUser(
            @Param("userId") Long userId,
            @Param("howLongAgo") LocalDateTime howLongAgo
    );

}
