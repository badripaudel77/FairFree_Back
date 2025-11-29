package com.app.fairfree.repository;

import com.app.fairfree.model.ItemImage;
import com.app.fairfree.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageItemRepository extends JpaRepository<ItemImage, Long> {

    // Find all images for a given item
    List<ItemImage> findByItem(Item item);

    // delete all images for a given item
    void deleteByItem(Item item);
}
