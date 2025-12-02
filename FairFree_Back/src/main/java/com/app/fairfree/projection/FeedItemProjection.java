package com.app.fairfree.projection;

import java.time.LocalDateTime;

public interface FeedItemProjection {

    Long getId();

    String getAuthorName();
    String getAuthorAvatarUrl();
    String getGroupName();      // currently null, OK

    LocalDateTime getCreatedAt();
    String getText();
    String getImageUrl();

    long getLikesCount();
    long getCommentsCount();
    boolean isLikedByCurrentUser();
}
