package com.app.fairfree.dto.feed;

import java.time.LocalDateTime;

public record FeedItemDto(
        Long id,                 // item id
        String authorName,       // "Helena"
        String authorAvatarUrl,  // avatar image
        String groupName,        // optional, can be null/empty for now
        LocalDateTime createdAt, // used by mobile to show "3 min ago"
        String text,             // body: "I have some flower for you today"
        String imageUrl,         // main image in the card
        long likesCount,         // "21 likes"
        long commentsCount,      // "4 comments"
        boolean likedByCurrentUser  // highlight like icon if true
) {}
