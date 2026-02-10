package com.marketmind.controller;

import com.marketmind.domain.OpportunityPriority;
import com.marketmind.domain.OpportunityStatus;
import com.marketmind.dto.*;
import com.marketmind.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/opportunities")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OpportunityController {
    
    private final OpportunityService opportunityService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<PageResponse<OpportunityDto>> getAllOpportunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(opportunityService.getAllOpportunities(page, size, sortBy, direction));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<OpportunityDto> getOpportunityById(@PathVariable Long id) {
        return opportunityService.getOpportunityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<OpportunityDto>> getOpportunitiesByStatus(@PathVariable OpportunityStatus status) {
        return ResponseEntity.ok(opportunityService.getOpportunitiesByStatus(status));
    }
    
    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<OpportunityDto>> getOpportunitiesByPriority(@PathVariable OpportunityPriority priority) {
        return ResponseEntity.ok(opportunityService.getOpportunitiesByPriority(priority));
    }
    
    @GetMapping("/high-scoring")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<OpportunityDto>> getHighScoringOpportunities(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(opportunityService.getHighScoringOpportunities(limit));
    }
    
    @GetMapping("/cluster/{clusterId}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<OpportunityDto>> getOpportunitiesByCluster(@PathVariable Long clusterId) {
        return ResponseEntity.ok(opportunityService.getOpportunitiesByCluster(clusterId));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<OpportunityDto> createOpportunity(
            @Valid @RequestBody CreateOpportunityRequest request,
            Authentication authentication) {
        String createdBy = authentication != null ? authentication.getName() : "system";
        return ResponseEntity.ok(opportunityService.createOpportunity(request, createdBy));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<OpportunityDto> updateOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOpportunityRequest request) {
        return ResponseEntity.ok(opportunityService.updateOpportunity(id, request));
    }
    
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<OpportunityDto> validateOpportunity(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String validatedBy = authentication != null ? authentication.getName() : "system";
        String notes = request.getOrDefault("notes", "");
        return ResponseEntity.ok(opportunityService.validateOpportunity(id, validatedBy, notes));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats/by-status")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getOpportunitiesByStatus() {
        return ResponseEntity.ok(opportunityService.getOpportunitiesByStatus());
    }
    
    @GetMapping("/stats/by-priority")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getOpportunitiesByPriority() {
        return ResponseEntity.ok(opportunityService.getOpportunitiesByPriority());
    }
    
    @GetMapping("/stats/average-scores")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, Double>> getAverageScores() {
        return ResponseEntity.ok(Map.of(
                "overall", opportunityService.getAverageOverallScore() != null ? opportunityService.getAverageOverallScore() : 0.0,
                "confidence", opportunityService.getAverageConfidenceScore() != null ? opportunityService.getAverageConfidenceScore() : 0.0,
                "marketPotential", opportunityService.getAverageMarketPotentialScore() != null ? opportunityService.getAverageMarketPotentialScore() : 0.0,
                "feasibility", opportunityService.getAverageFeasibilityScore() != null ? opportunityService.getAverageFeasibilityScore() : 0.0
        ));
    }
}
