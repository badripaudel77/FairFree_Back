package com.app.fairfree.repository;

import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.Item;
import com.app.fairfree.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(ItemStatus status);

    List<Item> findByOwner(User owner);

    @Query("SELECT i FROM Item i WHERE i.status <> :status AND i.owner.id <> :ownerId")
    List<Item> findAllItemsNotByUser(ItemStatus status, Long ownerId);

    List<Item> findByNeverExpiresFalseAndExpiresAfterDaysIsNotNullAndStatusNot(ItemStatus itemStatus);

    @Query("""
                SELECT i FROM Item i
                LEFT JOIN i.location l
                WHERE (LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(l.city) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(l.state) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(l.country) LIKE LOWER(CONCAT('%', :query, '%')))
                     AND i.status <> com.app.fairfree.enums.ItemStatus.DONATED
                     AND i.owner.id <> :userId
            """)
    List<Item> searchItems(@Param("query") String query, @Param("userId") Long userId);
}
