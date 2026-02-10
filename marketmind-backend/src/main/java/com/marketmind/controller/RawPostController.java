package com.marketmind.controller;

import com.marketmind.domain.PostSource;
import com.marketmind.domain.PostStatus;
import com.marketmind.dto.PageResponse;
import com.marketmind.dto.RawPostDto;
import com.marketmind.service.RawPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RawPostController {
    
    private final RawPostService rawPostService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<PageResponse<RawPostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "collectedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(rawPostService.getAllPosts(page, size, sortBy, direction));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<RawPostDto> getPostById(@PathVariable Long id) {
        return rawPostService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/external/{externalId}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<RawPostDto> getPostByExternalId(@PathVariable String externalId) {
        return rawPostService.getPostByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/source/{source}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<PageResponse<RawPostDto>> getPostsBySource(
            @PathVariable PostSource source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(rawPostService.getPostsBySource(source, page, size));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<PageResponse<RawPostDto>> getPostsByStatus(
            @PathVariable PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(rawPostService.getPostsByStatus(status, page, size));
    }
    
    @GetMapping("/without-embeddings")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<List<RawPostDto>> getPostsWithoutEmbeddings() {
        return ResponseEntity.ok(rawPostService.getPostsWithoutEmbeddings());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<RawPostDto>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(rawPostService.searchPosts(query));
    }
    
    @GetMapping("/stats/by-source")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getPostsBySource() {
        return ResponseEntity.ok(rawPostService.getPostsBySource());
    }
    
    @GetMapping("/stats/by-status")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getPostsByStatus() {
        return ResponseEntity.ok(rawPostService.getPostsByStatus());
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<Void> updatePostStatus(@PathVariable Long id, @RequestParam PostStatus status) {
        rawPostService.updatePostStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
