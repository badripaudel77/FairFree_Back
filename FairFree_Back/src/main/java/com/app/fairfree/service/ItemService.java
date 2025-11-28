package com.app.fairfree.service;

import com.app.fairfree.model.Item;
import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public Item addItem(User user, String name, String location, String imageUrl, Integer expiresAfterDays, boolean neverExpires) {
        if (neverExpires) {
            expiresAfterDays = Integer.MAX_VALUE; // never expires
        }

        Item item = Item.builder()
                .name(name)
                .location(location)
                .imageUrl(imageUrl)
                .status(ItemStatus.PRIVATE)
                .owner(user)
                .expiresAfterDays(expiresAfterDays)
                .build();
        return itemRepository.save(item);
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
