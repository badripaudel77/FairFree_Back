package com.app.fairfree.service;

import com.app.fairfree.dto.feed.FeedResponse;

public interface FeedService {

    /**
     * Build a feed for the given user and view mode.
     * view examples: "for_you", "following", "nearby"
     */
    FeedResponse getFeedForUser(Long userId, String view, int page, int size);
}
