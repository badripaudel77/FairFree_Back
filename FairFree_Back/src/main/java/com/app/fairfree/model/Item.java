package com.app.fairfree.model;

import com.app.fairfree.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private String description;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> images = new ArrayList<>();

    // latitude / longitude (for location queries & map display)
    private Double latitude;
    private Double longitude;

    private String location; // location of the item (to show on map).

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private Integer expiresAfterDays; // Expiration of the item, can be used for reminders email
    private Boolean neverExpires = false;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner; // Who added the item

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    /** Category for feed UI */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Likes associated with the item */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    /** Comments associated with the item */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = ItemStatus.PRIVATE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
