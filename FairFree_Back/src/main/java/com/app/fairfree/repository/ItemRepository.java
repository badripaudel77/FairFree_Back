package com.app.fairfree.repository;

import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.Item;
import com.app.fairfree.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(ItemStatus status);
    List<Item> findByOwner(User owner);
}
