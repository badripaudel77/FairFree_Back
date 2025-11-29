package com.app.fairfree.api;

import com.app.fairfree.dto.ItemRequest;
import com.app.fairfree.dto.ItemResponse;
import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.Item;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.UserRepository;
import com.app.fairfree.service.ItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    // Authenticated user can add item.
    // Add item for tracking and for donation.
    // Add Item, including upto three images.
    // When multipart data is added, itemToBeAdded is String, otherwise client tool may set content type different causing problem.
    // @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart String itemToBeAddedString,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws JsonProcessingException {
        // Passing @AuthenticationPrincipal UserDetails userDetails, is same as this.
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ObjectMapper mapper = new ObjectMapper();
        ItemRequest itemToBeAdded = mapper.readValue(itemToBeAddedString, ItemRequest.class);

        ItemResponse item = itemService.addItem(user, itemToBeAdded, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
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
