package com.app.fairfree.api;

import com.app.fairfree.dto.ItemRequest;
import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.Item;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.UserRepository;
import com.app.fairfree.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    //  Add Item
    @PostMapping("")
    public ResponseEntity<Item> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ItemRequest itemToBeAdded
            ) {
        // Passing @AuthenticationPrincipal UserDetails userDetails, is same as this.
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // authentication.getName();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Item item = itemService.addItem(user, itemToBeAdded.name(), itemToBeAdded.location(), itemToBeAdded.imageUrl(), itemToBeAdded.expiresAfterDays(), itemToBeAdded.neverExpires());
        return ResponseEntity.ok(item);
    }

    //  Get All Available Items
    @GetMapping("/available")
    public ResponseEntity<List<Item>> getAllAvailableItems() {
        return ResponseEntity.ok(itemService.getAllAvailableItems());
    }

    // Get Items by specific User
    @GetMapping("/my-items")
    public ResponseEntity<List<Item>> getItemsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(itemService.getItemsByUser(user));
    }

    //Get Item by ID
    @GetMapping("/{itemId}")
    public ResponseEntity<Item> getItemById(@PathVariable Long itemId) {
        return itemService.getItemById(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update Item Status
    @PatchMapping("/{itemId}/status")
    public ResponseEntity<Item> updateItemStatus(
            @PathVariable Long itemId,
            @RequestParam ItemStatus status
    ) {
        Item updatedItem = itemService.updateStatus(itemId, status);
        return ResponseEntity.ok(updatedItem);
    }

    // Delete Item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        itemService.deleteItem(itemId, user);
        return ResponseEntity.ok("Item deleted successfully");
    }

}
