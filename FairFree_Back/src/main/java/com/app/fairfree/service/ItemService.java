package com.app.fairfree.service;

import com.app.fairfree.dto.ItemRequest;
import com.app.fairfree.dto.ItemResponse;
import com.app.fairfree.model.Item;
import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.ItemImage;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.ImageItemRepository;
import com.app.fairfree.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final FileService fileService;
    private final ImageItemRepository imageItemRepository;

    @Transactional
    public ItemResponse addItem(User user, ItemRequest itemRequest, List<MultipartFile> images) {
        int expiresAfterDays = itemRequest.neverExpires() ? Integer.MAX_VALUE : itemRequest.expiresAfterDays();

        Item item = Item.builder()
                .name(itemRequest.name())
                .description(itemRequest.description())
                .location(itemRequest.location())
                .latitude(itemRequest.latitude())
                .longitude(itemRequest.longitude())
                .status(ItemStatus.PRIVATE)
                .owner(user)
                .receiver(null)
                .expiresAfterDays(expiresAfterDays)
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

        return new ItemResponse(
                savedItem.getId(),
                savedItem.getName(),
                savedItem.getLocation(),
                imageUrls,             // pass only URLs to frontend
                savedItem.getStatus(),
                savedItem.getOwner().getId(),
                null,
                savedItem.getOwner().getFullName(),
                savedItem.getExpiresAfterDays(),
                savedItem.getNeverExpires()
        );
    }

    public List<Item> getAllAvailableItems() {
        return itemRepository.findByStatus(ItemStatus.AVAILABLE);
    }

    @Transactional
    public Item updateStatus(Long itemId, ItemStatus newStatus) {
        return itemRepository.findById(itemId)
                        .map(item -> {
                           item.setStatus(newStatus);
                           return item;
                        })
                .orElseThrow(() -> new RuntimeException("Item Not found."));
    }

    @Transactional
    public Item deleteItem(Long itemId, User owner) {
        return itemRepository.findById(itemId)
                .map(item -> {
                    if (!item.getOwner().getId().equals(owner.getId())) {
                        throw new RuntimeException("You are not authorized to delete this item");
                    }
                    // Get all image keys associated with the item
                    List<String> imageKeys = item.getImages().stream()
                            .map(ItemImage::getImageKey)
                            .toList();
                    // Delete images from S3
                    fileService.deleteImages(imageKeys);

                    // Delete item from DB (this will cascade to ItemImage if properly mapped)
                    itemRepository.delete(item);
                    return item;
                })
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public Optional<Item> getItemById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    public List<Item> getItemsByUser(User user) {
        return itemRepository.findByOwner(user);
    }
}
