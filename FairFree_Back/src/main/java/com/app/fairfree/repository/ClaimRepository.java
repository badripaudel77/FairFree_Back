package com.app.fairfree.repository;

import com.app.fairfree.model.Claim;
import com.app.fairfree.model.Item;
import com.app.fairfree.model.User;
import com.app.fairfree.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // Find all claims for a specific item
    List<Claim> findByItem(Item item);

    // Find all claims by a specific user
    List<Claim> findByUser(User user);

    // Find all claims by item and status (e.g., PENDING)
    List<Claim> findByItemAndStatus(Item item, ClaimStatus status);

    // Optional: find specific claim by item and user
    Optional<Claim> findByItemAndUser(Item item, User user);
}
