package com.app.fairfree.api;

import com.app.fairfree.dto.ClaimResponse;
import com.app.fairfree.dto.ItemRequest;
import com.app.fairfree.dto.ItemResponse;
import com.app.fairfree.enums.ItemStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping(value = "/api/{version}/items", version = "1")
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
    public ResponseEntity<List<ItemResponse>> getAllAvailableItems() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        return ResponseEntity.ok(itemService.getAllAvailableItems(authentication.getName()));
    }

    // Get Items by specific User
    @GetMapping("/my-items")
    public ResponseEntity<List<ItemResponse>> getItemsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(itemService.getItemsByUser(user));
    }

    //Get Item by ID
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long itemId) {
        return itemService.getItemById(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponse>> searchItems(@RequestParam("searchText") String query, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ItemResponse> results = itemService.findMatchingItems(query, user.getId());
        return ResponseEntity.ok(results);
    }

    // Update Item Status
    @PatchMapping("/{itemId}/status")
    public ResponseEntity<ItemResponse> updateItemStatus(
            @PathVariable Long itemId,
            @RequestParam ItemStatus status
    ) {
        ItemResponse updatedItem = itemService.updateStatus(itemId, status);
        return ResponseEntity.ok(updatedItem);
    }

    // Delete Item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Boolean> deleteItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(itemService.deleteItem(itemId, user));
    }

    @PostMapping("/{itemId}/claim")
    public ResponseEntity<ClaimResponse> claimItem(
            @PathVariable Long itemId, @AuthenticationPrincipal UserDetails claimant) {
        ClaimResponse response = itemService.claimItem(itemId, claimant);
        return ResponseEntity.ok(response);
    }

    // Decline the claim ID
    @PostMapping("/decline/claims/{claimId}")
    public ResponseEntity<ClaimResponse> declineClaim(@PathVariable Long claimId, @AuthenticationPrincipal UserDetails owner) {
        ClaimResponse response = itemService.declineClaim(claimId, owner);
        return ResponseEntity.ok(response);
    }

    // Decline the claim ID
    @PostMapping("/approve/claims/{claimId}")
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable Long claimId, @AuthenticationPrincipal UserDetails owner) {
        ClaimResponse response = itemService.approveClaim(claimId, owner);
        return ResponseEntity.ok(response);
    }

    // Cancel the claim (owned claim)
    @PostMapping("/cancel/claims/{claimId}")
    public ResponseEntity<ClaimResponse> cancelClaim(@PathVariable Long claimId, @AuthenticationPrincipal UserDetails owner) {
        ClaimResponse response = itemService.cancelClaim(claimId, owner);
        return ResponseEntity.ok(response);
    }

    // Get the claims (owned claims)
    @GetMapping("/my-claims")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(@AuthenticationPrincipal UserDetails owner) {
        List<ClaimResponse> claims = itemService.getClaims(owner);
        return ResponseEntity.ok(claims);
    }

}
