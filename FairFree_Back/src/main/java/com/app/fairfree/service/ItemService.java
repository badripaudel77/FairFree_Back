package com.app.fairfree.service;

import com.app.fairfree.dto.*;
import com.app.fairfree.enums.ClaimStatus;
import com.app.fairfree.model.*;
import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.repository.ClaimRepository;
import com.app.fairfree.repository.ImageItemRepository;
import com.app.fairfree.repository.ItemRepository;
import com.app.fairfree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${notification.items.expiring-days}")
    private int expiringDays;

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
                        // Notify the claimants if that item was removed or deleted.
                        notificationService.sendEmailNotification(claim.getUser().getEmail(),
                                "Item has been deleted by the owner", claim.getItem().getOwner().getFullName()
                                        + " no longer donating the item you claimed.");
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

    // Claim the Item
    @Transactional
    public ClaimResponse claimItem(Long itemId, UserDetails claimant) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getOwner().getEmail().equals(claimant.getUsername())) {
            throw new RuntimeException("Owner cannot claim their own item");
        }
        Optional<User> userByEmail = userRepository.findByEmail(claimant.getUsername());
        Claim claim = Claim.builder()
                .item(item)
                .user(userByEmail.get())
                .status(ClaimStatus.PENDING)
                .build();
        item.getClaims().add(claim);
        claimRepository.save(claim);
        if (item.getStatus() == ItemStatus.AVAILABLE) {
            item.setStatus(ItemStatus.ON_HOLD);
            itemRepository.save(item);
        }
        // Notify the owner that someone has claimed it.
        notificationService.sendEmailNotification(claim.getItem().getOwner().getEmail(),
                "Your item has been claimed", claim.getUser().getFullName() + " claimed for your donation.");
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse declineClaim(Long claimId, UserDetails owner) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        Item item = claim.getItem();
        if (!item.getOwner().getEmail().equals(owner.getUsername())) {
            throw new RuntimeException("Not authorized to decline this claim");
        }
        if(claim.getStatus() != ClaimStatus.PENDING) {
            throw new RuntimeException("Can't decline, the claim is not in PENDING status.");
        }
        claim.setStatus(ClaimStatus.DECLINED);
        claimRepository.save(claim);
        // Check if item should go back to AVAILABLE, If no other pending, take back to available.
        boolean hasPending = item.getClaims().stream()
                .anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
        if (!hasPending && item.getStatus() == ItemStatus.ON_HOLD) {
            item.setStatus(ItemStatus.AVAILABLE);
            itemRepository.save(item);
        }
        // Notify the claimer that owner has declined your claim
        notificationService.sendEmailNotification(claim.getItem().getOwner().getEmail(),
                "Claim Declined by the owner", claim.getItem().getOwner().getFullName() + " denied your claim.");

        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse approveClaim(Long claimId, UserDetails owner) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        Item item = claim.getItem();
        if (!item.getOwner().getEmail().equals(owner.getUsername())) {
            throw new RuntimeException("Not authorized to approve this claim");
        }
        // Approve this claim
        claim.setStatus(ClaimStatus.APPROVED);
        // Decline all other PENDING claims
        item.getClaims().stream()
                .filter(c -> !c.getId().equals(claimId) && c.getStatus() == ClaimStatus.PENDING)
                .forEach(c -> c.setStatus(ClaimStatus.DECLINED));
        // Update item
        item.setStatus(ItemStatus.DONATED);
        item.setReceiver(claim.getUser());
        claimRepository.save(claim);
        itemRepository.save(item);
        // Notify claimants, approval for the one with approved and declined for others
        for (Claim c : item.getClaims()) {
            if (Objects.equals(c.getId(), claim.getId())) {
                notificationService.sendEmailNotification(claim.getUser().getEmail(),
                        "Your claim has been approved", claim.getItem().getOwner().getFullName()
                                + " has approved your claim. Please pick it up from the location.");
            }
            else {
                notificationService.sendEmailNotification(claim.getUser().getEmail(),
                        "Your claim was not approved", claim.getItem().getOwner().getFullName()
                                + " didn't approve your claim.");
            }
        }
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse cancelClaim(Long claimId, UserDetails user) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        if (!claim.getUser().getEmail().equals(user.getUsername())) {
            throw new RuntimeException("Not authorized to cancel this claim");
        }
        claim.setStatus(ClaimStatus.CANCELLED);
        claimRepository.save(claim);

        Item item = claim.getItem();
        boolean hasPending = item.getClaims().stream()
                .anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
        if (!hasPending && item.getStatus() == ItemStatus.ON_HOLD) {
            item.setStatus(ItemStatus.AVAILABLE);
            itemRepository.save(item);
        }
        // Notify the owner that user has canceled their clam.
        notificationService.sendEmailNotification(claim.getItem().getOwner().getEmail(),
                "User removed their claim for the item.", claim.getUser().getFullName() + " removed their claim for the item.");
        return ClaimResponse.from(claim);
    }

    // Get the claims of the logged in User
    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaims(UserDetails user) {
        Optional<User> byEmail = userRepository.findByEmail(user.getUsername());
        List<Claim> claims = claimRepository.findByUser(byEmail.get());
        return Optional.ofNullable(claims)
                .orElse(Collections.emptyList())
                .stream()
                .map(ClaimResponse::from)
                .toList();
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

    public List<Item> getExpiringItems() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(expiringDays);

        List<Item> items = itemRepository
                .findByNeverExpiresFalseAndExpiresAfterDaysIsNotNull();

        return items.stream()
                .filter(item -> {
                    LocalDateTime expirationDate =
                            item.getCreatedAt().plusDays(item.getExpiresAfterDays());
                    return expirationDate.isAfter(now) && expirationDate.isBefore(future);
                })
                .toList();
    }
}
