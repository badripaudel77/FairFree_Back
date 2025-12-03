package com.app.fairfree.dto.feed;

import com.app.fairfree.dto.feed.FeedItemDto;

import java.util.List;

public record FeedResponse(
        List<FeedItemDto> items,
        int page,
        int totalPages,
        long totalItems
) {}
