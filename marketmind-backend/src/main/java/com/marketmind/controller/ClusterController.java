package com.marketmind.controller;

import com.marketmind.dto.ClusterDto;
import com.marketmind.dto.PageResponse;
import com.marketmind.service.ClusterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClusterController {
    
    private final ClusterService clusterService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<PageResponse<ClusterDto>> getAllClusters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(clusterService.getAllClusters(page, size, activeOnly));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<ClusterDto> getClusterById(@PathVariable Long id) {
        return clusterService.getClusterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/label/{label}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<ClusterDto> getClusterByLabel(@PathVariable String label) {
        return clusterService.getClusterByLabel(label)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/top")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<ClusterDto>> getTopClusters(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(clusterService.getTopClusters(limit));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<ClusterDto>> getClustersByTerm(@RequestParam String term) {
        return ResponseEntity.ok(clusterService.getClustersByTerm(term));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ClusterDto> createCluster(@RequestBody ClusterDto dto) {
        return ResponseEntity.ok(clusterService.createCluster(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ClusterDto> updateCluster(@PathVariable Long id, @RequestBody ClusterDto dto) {
        return ResponseEntity.ok(clusterService.updateCluster(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCluster(@PathVariable Long id) {
        clusterService.deleteCluster(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/run-clustering")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> runClustering() {
        clusterService.performClustering();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats/count")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Long> countActiveClusters() {
        return ResponseEntity.ok(clusterService.countActiveClusters());
    }
    
    @GetMapping("/stats/avg-posts")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Double> getAveragePostsPerCluster() {
        return ResponseEntity.ok(clusterService.getAveragePostsPerCluster());
    }
}
