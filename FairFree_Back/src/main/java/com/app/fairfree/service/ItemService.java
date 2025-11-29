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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final FileService fileService;
    private final ImageItemRepository imageItemRepository;

    @Transactional
    public ItemResponse addItem(User user, ItemRequest itemRequest, List<MultipartFile> images) {
        int expiresAfterDays;
        if (itemRequest.neverExpires()) {
            expiresAfterDays = Integer.MAX_VALUE;
        }
        else {
            expiresAfterDays = itemRequest.expiresAfterDays();
        }
        // Upload each file to S3 and collect image URLs
        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = fileService.uploadImagesToCloud(file);
                uploadedImageUrls.add(url);
            }
        }
        // Create Item entity
        Item item = Item.builder()
                .name(itemRequest.name())
                .description(itemRequest.description())
                .location(itemRequest.location())
                .latitude(itemRequest.latitude())
                .longitude(itemRequest.longitude())
                .status(ItemStatus.PRIVATE)
                .owner(user)
                .receiver(null) // when created, by default it is null.
                .expiresAfterDays(expiresAfterDays)
                .build();

        Item savedItem = itemRepository.save(item);
        // Saving uploaded image URLs in a separate ItemImage table
        if (!uploadedImageUrls.isEmpty()) {
            List<ItemImage> imageEntities = uploadedImageUrls.stream()
                    .map(url -> ItemImage.builder()
                            .imageUrl(url)
                            .item(savedItem)
                            .build())
                    .toList();
            imageItemRepository.saveAll(imageEntities);

            List<String> urls = imageEntities.stream()
                    .map(ItemImage::getImageUrl)
                    .toList();
            savedItem.setImageUrls(urls);
        }
        ItemResponse response = new ItemResponse(savedItem.getId(), savedItem.getName(), savedItem.getLocation(),
                savedItem.getImageUrls(), savedItem.getStatus(), savedItem.getOwner().getId(), null,
                savedItem.getOwner().getFullName(), savedItem.getExpiresAfterDays(), savedItem.getNeverExpires());
        return response;
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
