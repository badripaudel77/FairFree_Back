package com.app.fairfree.api;

import com.app.fairfree.dto.feed.FeedResponse;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.UserRepository;
import com.app.fairfree.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final AuthController authController;

    @GetMapping
    public ResponseEntity<FeedResponse> getFeed(
            @RequestParam(defaultValue = "for_you") String view,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long currentUserId =  authController.getCurrentUserId();
        FeedResponse feed = feedService.getFeedForUser(currentUserId, view, page, size);
        return ResponseEntity.ok(feed);
    }


}
