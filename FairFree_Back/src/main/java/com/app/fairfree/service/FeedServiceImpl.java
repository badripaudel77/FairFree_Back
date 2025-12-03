package com.app.fairfree.service;

import com.app.fairfree.dto.feed.FeedItemDto;
import com.app.fairfree.dto.feed.FeedResponse;
import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.projection.FeedItemProjection;
import com.app.fairfree.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;

    @Override
    public FeedResponse getFeedForUser(Long userId, String view, int page, int size) {
        if (view == null || view.isBlank()) {
            view = "for_you";
        }

        var pageable = PageRequest.of(page, size);

        ItemStatus visibleStatus = ItemStatus.AVAILABLE;

        Page<FeedItemProjection> projPage = switch (view) {
            case "following" -> feedRepository.findFollowingFeed(userId, visibleStatus, pageable);
            default          -> feedRepository.findForYouFeed(userId, visibleStatus, pageable);
        };

        List<FeedItemDto> items = projPage
                .map(this::toDto)
                .toList();

        return new FeedResponse(
                items,
                projPage.getNumber(),
                projPage.getTotalPages(),
                projPage.getTotalElements()
        );
    }

    private FeedItemDto toDto(FeedItemProjection p) {
        return new FeedItemDto(
                p.getId(),
                p.getAuthorName(),
                p.getAuthorAvatarUrl(),
                p.getGroupName(),
                p.getCreatedAt(),
                p.getText(),
                p.getImageUrl(),
                p.getLikesCount(),
                p.getCommentsCount(),
                p.isLikedByCurrentUser()
        );
    }
}
