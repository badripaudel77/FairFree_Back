package com.app.fairfree.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fairfield, IA
    private String address;

   // Fairfield
    private String city;

    private String state;
    private String country;

    /** Geo coordinates */
    private Double latitude;
    private Double longitude;

    @OneToOne(mappedBy = "location")
    private Item item;
}
