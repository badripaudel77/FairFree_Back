package com.app.fairfree.model;

import com.app.fairfree.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * This class is for holding info about the item to be tracked / donated.
 */
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String imageUrl; // image url, ref to actual file.
    private String location; // location of the item (to show on map).

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private Integer expiresAfterDays; // Expiration of the item, can be used for reminders email


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner; // Who added the item


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
