package com.app.fairfree.repository;

import com.app.fairfree.enums.ItemStatus;
import com.app.fairfree.model.Item;
import com.app.fairfree.projection.FeedItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Item, Long> {

    @Query("""
        SELECT 
            i.id                        AS id,
            o.fullName                  AS authorName,
            o.avatarUrl                 AS authorAvatarUrl,
            null                        AS groupName,
            i.createdAt                 AS createdAt,
            i.description               AS text,
            img.imageUrl                AS imageUrl,
            (SELECT COUNT(l) FROM Like l WHERE l.item = i)            AS likesCount,
            (SELECT COUNT(c) FROM Comment c WHERE c.item = i)         AS commentsCount,
            (SELECT CASE WHEN COUNT(l2) > 0 THEN true ELSE false END
               FROM Like l2
               WHERE l2.item = i AND l2.user.id = :currentUserId)    AS likedByCurrentUser
        FROM Item i
            JOIN i.owner o
            LEFT JOIN i.images img ON img.isThumbnail = true
        WHERE i.status = :status
        ORDER BY i.createdAt DESC
        """)
    Page<FeedItemProjection> findForYouFeed(@Param("currentUserId") Long currentUserId,
                                            @Param("status") ItemStatus status,
                                            Pageable pageable);

    @Query("""
        SELECT 
            i.id                        AS id,
            o.fullName                  AS authorName,
            o.avatarUrl                 AS authorAvatarUrl,
            null                        AS groupName,
            i.createdAt                 AS createdAt,
            i.description               AS text,
            img.imageUrl                AS imageUrl,
            (SELECT COUNT(l) FROM Like l WHERE l.item = i)            AS likesCount,
            (SELECT COUNT(c) FROM Comment c WHERE c.item = i)         AS commentsCount,
            (SELECT CASE WHEN COUNT(l2) > 0 THEN true ELSE false END
               FROM Like l2
               WHERE l2.item = i AND l2.user.id = :currentUserId)    AS likedByCurrentUser
        FROM Item i
            JOIN i.owner o
            LEFT JOIN i.images img ON img.isThumbnail = true
        WHERE i.status = :status
        ORDER BY i.createdAt DESC
        """)
    Page<FeedItemProjection> findFollowingFeed(@Param("currentUserId") Long currentUserId,
                                               @Param("status") ItemStatus status,
                                               Pageable pageable);
}
