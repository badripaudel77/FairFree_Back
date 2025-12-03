package com.app.fairfree.service;

import com.app.fairfree.dto.*;
import com.app.fairfree.model.*;
import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.repository.ImageItemRepository;
import com.app.fairfree.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final FileService fileService;
    private final ImageItemRepository imageItemRepository;

    @Transactional
    public ItemResponse addItem(User user, ItemRequest itemRequest, List<MultipartFile> images) {
        int expiresAfterDays = itemRequest.neverExpires() ? Integer.MAX_VALUE : itemRequest.expiresAfterDays();
            // Build Location entity
            ItemLocation location = ItemLocation.builder()
                .address(itemRequest.location().address())
                .city(itemRequest.location().city())
                .state(itemRequest.location().state())
                .country(itemRequest.location().country())
                .latitude(itemRequest.location().latitude())
                .longitude(itemRequest.location().longitude())
                .build();

            // Build Item entity
            Item item = Item.builder()
                .title(itemRequest.title())
                .description(itemRequest.description())
                .quantity(itemRequest.quantity())
                .status(ItemStatus.PRIVATE) // by default private.
                .owner(user)
                .receiver(null)
                .neverExpires(itemRequest.neverExpires())
                .expiresAfterDays(itemRequest.neverExpires() ? null : itemRequest.expiresAfterDays())
                .location(location)
                .build();

        // Save item first to get its ID for S3 folder organization
        Item savedItem = itemRepository.save(item);

        // Upload images to S3 GET URLS
        List<ItemImage> imageEntities = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                // Upload image to S3; returns a map with 'key' and 'url'
                Map<String, String> map = fileService.uploadImage(file, savedItem.getId());
                ItemImage image = ItemImage.builder()
                        .imageUrl(map.get("url"))
                        .imageKey(map.get("key"))   // S3 key for IMAGE TRACKING
                        .item(savedItem)
                        .build();
                imageEntities.add(image);
            }
            // Save images to DB
            imageItemRepository.saveAll(imageEntities);
            // Assign images to item entity (for JPA mapping)
            savedItem.setImages(imageEntities);
        }
        List<String> imageUrls = savedItem.getImages().stream()
                .map(ItemImage::getImageUrl)
                .toList();

        List<ClaimResponse> claimResponses = Optional.ofNullable(savedItem.getClaims())
                .orElse(Collections.emptyList())
                .stream()
                .map(ClaimResponse::from)
                .toList();
        UserResponse ownerResponse = new UserResponse(
                savedItem.getOwner().getId(),
                savedItem.getOwner().getFullName(),
                savedItem.getOwner().getEmail()
        );

        return new ItemResponse(
                savedItem.getId(),
                savedItem.getTitle(),
                savedItem.getDescription(),
                savedItem.getQuantity(),
                LocationResponse.from(savedItem.getLocation()),
                imageUrls,
                savedItem.getStatus(),
                ownerResponse,  // pass owner object
                null,           // receiver
                savedItem.getExpiresAfterDays(),
                savedItem.getNeverExpires(),
                claimResponses
        );

    }

    public List<ItemResponse> getAllAvailableItems() {
        List<Item> items = itemRepository.findByStatus(ItemStatus.AVAILABLE);

        return items.stream().map(item -> {
            List<String> imageUrls = Optional.ofNullable(item.getImages())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(ItemImage::getImageUrl)
                    .toList();

            List<ClaimResponse> claimResponses = Optional.ofNullable(item.getClaims())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(ClaimResponse::from)
                    .toList();

            UserResponse ownerResponse = new UserResponse(
                    item.getOwner().getId(),
                    item.getOwner().getFullName(),
                    item.getOwner().getEmail()
            );

            UserResponse receiverResponse = item.getReceiver() != null
                    ? new UserResponse(item.getReceiver().getId(), item.getReceiver().getFullName(), item.getReceiver().getEmail())
                    : null;

            return new ItemResponse(
                    item.getId(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getQuantity(),
                    LocationResponse.from(item.getLocation()),
                    imageUrls,
                    item.getStatus(),
                    ownerResponse,
                    receiverResponse,
                    item.getExpiresAfterDays(),
                    item.getNeverExpires(),
                    claimResponses
            );
        }).toList();
    }


    @Transactional
    public ItemResponse updateStatus(Long itemId, ItemStatus newStatus) {
        Item updatedItem = itemRepository.findById(itemId)
                .map(item -> {
                    item.setStatus(newStatus);
                    return item;
                })
                .orElseThrow(() -> new RuntimeException("Item Not found."));

        // Convert updated entity to ItemResponse
        List<String> imageUrls = Optional.ofNullable(updatedItem.getImages())
                .orElse(Collections.emptyList())
                .stream()
                .map(ItemImage::getImageUrl)
                .toList();

        List<ClaimResponse> claimResponses = Optional.ofNullable(updatedItem.getClaims())
                .orElse(Collections.emptyList())
                .stream()
                .map(ClaimResponse::from)
                .toList();

        UserResponse ownerResponse = UserResponse.from(updatedItem.getOwner());
        UserResponse receiverResponse = updatedItem.getReceiver() != null
                ? UserResponse.from(updatedItem.getReceiver())
                : null;

        return new ItemResponse(
                updatedItem.getId(),
                updatedItem.getTitle(),
                updatedItem.getDescription(),
                updatedItem.getQuantity(),
                LocationResponse.from(updatedItem.getLocation()),
                imageUrls,
                updatedItem.getStatus(),
                ownerResponse,
                receiverResponse,
                updatedItem.getExpiresAfterDays(),
                updatedItem.getNeverExpires(),
                claimResponses
        );
    }

    @Transactional
    public Boolean deleteItem(Long itemId, User owner) {
        itemRepository.findById(itemId)
                .map(item -> {
                    if (!item.getOwner().getId().equals(owner.getId())) {
                        throw new RuntimeException("You are not authorized to delete this item");
                    }
                    // notify claimants of this itme
                    List<Claim> claims = Optional.ofNullable(item.getClaims())
                            .orElse(Collections.emptyList());
                    for (Claim claim : claims) {
                        // TODO: Notify the claimants if needed.
                    }
                    // Get all image keys associated with the item
                    List<String> imageKeys = Optional.ofNullable(item.getImages())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(ItemImage::getImageKey)
                            .toList();
                    // Delete images from S3
                    fileService.deleteImages(imageKeys);
                    // Delete item from DB (this will cascade to ItemImage and Claim)
                    itemRepository.delete(item);
                    return item;
                })
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return true;
    }


    public Optional<ItemResponse> getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .map(item -> {
                    List<String> imageUrls = Optional.ofNullable(item.getImages())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(ItemImage::getImageUrl)
                            .toList();
                    List<ClaimResponse> claimResponses = Optional.ofNullable(item.getClaims())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(ClaimResponse::from)
                            .toList();

                    UserResponse ownerResponse = UserResponse.from(item.getOwner());
                    UserResponse receiverResponse = item.getReceiver() != null
                            ? UserResponse.from(item.getReceiver())
                            : null;
                    return new ItemResponse(
                            item.getId(),
                            item.getTitle(),
                            item.getDescription(),
                            item.getQuantity(),
                            LocationResponse.from(item.getLocation()),
                            imageUrls,
                            item.getStatus(),
                            ownerResponse,
                            receiverResponse,
                            item.getExpiresAfterDays(),
                            item.getNeverExpires(),
                            claimResponses
                    );
                });
    }

    public List<ItemResponse> getItemsByUser(User user) {
        List<Item> items = itemRepository.findByOwner(user);

        return items.stream().map(item -> {
            List<String> imageUrls = Optional.ofNullable(item.getImages())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(ItemImage::getImageUrl)
                    .toList();
            List<ClaimResponse> claimResponses = Optional.ofNullable(item.getClaims())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(ClaimResponse::from)
                    .toList();
            UserResponse ownerResponse = new UserResponse(
                    item.getOwner().getId(),
                    item.getOwner().getFullName(),
                    item.getOwner().getEmail()
            );
            UserResponse receiverResponse = item.getReceiver() != null
                    ? new UserResponse(item.getReceiver().getId(), item.getReceiver().getFullName(), item.getReceiver().getEmail())
                    : null;

            return new ItemResponse(
                    item.getId(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getQuantity(),
                    LocationResponse.from(item.getLocation()),
                    imageUrls,
                    item.getStatus(),
                    ownerResponse,
                    receiverResponse,
                    item.getExpiresAfterDays(),
                    item.getNeverExpires(),
                    claimResponses
            );
        }).toList();
    }


    @Value("${notification.items.expiring-days}")
    private int expiringDays;

    public List<Item> getExpiringItems() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(expiringDays);

        List<Item> items = itemRepository
                .findByNeverExpiresFalseAndExpiresAfterDaysIsNotNullAndStatusNot(ItemStatus.EXPIRED);

        return items.stream()
                .filter(item -> {
                    LocalDateTime expirationDate =
                            item.getCreatedAt().plusDays(item.getExpiresAfterDays());
                    return expirationDate.isAfter(now) && expirationDate.isBefore(future);
                })
                .toList();
    }
}
