package com.app.fairfree.repository;

import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.Item;
import com.app.fairfree.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(ItemStatus status);
    List<Item> findByOwner(User owner);

    @Query("SELECT i FROM Item i WHERE i.status <> :status AND i.owner.id <> :ownerId")
    List<Item> findAllItemsNotByUser(ItemStatus status, Long ownerId);

    List<Item> findByNeverExpiresFalseAndExpiresAfterDaysIsNotNullAndStatusNot(ItemStatus itemStatus);
}
