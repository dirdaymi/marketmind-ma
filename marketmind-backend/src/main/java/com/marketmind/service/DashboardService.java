package com.marketmind.service;

import com.marketmind.dto.DashboardStatsDto;
import com.marketmind.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final RawPostRepository rawPostRepository;
    private final ClusterRepository clusterRepository;
    private final OpportunityRepository opportunityRepository;
    private final CollectionJobRepository collectionJobRepository;
    
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime last24Hours = now.minusHours(24);
        ZonedDateTime last7Days = now.minusDays(7);
        ZonedDateTime last30Days = now.minusDays(30);
        
        DashboardStatsDto stats = new DashboardStatsDto();
        
        // Post statistics
        stats.setTotalPosts(rawPostRepository.count());
        stats.setPostsLast24Hours(rawPostRepository.countSince(last24Hours));
        stats.setPostsLast7Days(rawPostRepository.countSince(last7Days));
        stats.setPostsLast30Days(rawPostRepository.countSince(last30Days));
        stats.setPostsBySource(getPostsBySource());
        stats.setPostsByStatus(getPostsByStatus());
        
        // Cluster statistics
        stats.setTotalClusters(clusterRepository.count());
        stats.setActiveClusters(clusterRepository.countActive());
        stats.setAveragePostsPerCluster(clusterRepository.averagePostCount());
        
        // Opportunity statistics
        stats.setTotalOpportunities(opportunityRepository.count());
        Map<String, Long> oppByStatus = getOpportunitiesByStatus();
        stats.setOpportunitiesByStatusDraft(oppByStatus.getOrDefault("DRAFT", 0L));
        stats.setOpportunitiesByStatusValidated(oppByStatus.getOrDefault("VALIDATED", 0L));
        stats.setOpportunitiesByStatusInProgress(oppByStatus.getOrDefault("IN_PROGRESS", 0L));
        stats.setOpportunitiesByStatusImplemented(oppByStatus.getOrDefault("IMPLEMENTED", 0L));
        stats.setAverageOpportunityScore(opportunityRepository.averageOverallScore());
        stats.setAverageConfidenceScore(opportunityRepository.averageConfidenceScore());
        stats.setAverageMarketPotentialScore(opportunityRepository.averageMarketPotentialScore());
        stats.setAverageFeasibilityScore(opportunityRepository.averageFeasibilityScore());
        
        // Collection statistics
        stats.setTotalCollectionJobs(collectionJobRepository.count());
        Long totalCollected = collectionJobRepository.totalPostsCollected();
        stats.setTotalPostsCollected(totalCollected != null ? totalCollected : 0L);
        Long totalNew = collectionJobRepository.totalPostsNew();
        stats.setTotalPostsNew(totalNew != null ? totalNew : 0L);
        
        return stats;
    }
    
    private Map<String, Long> getPostsBySource() {
        Map<String, Long> result = new HashMap<>();
        try {
            rawPostRepository.countBySource().forEach(arr -> {
                String source = ((com.marketmind.domain.PostSource) arr[0]).name();
                Long count = (Long) arr[1];
                result.put(source, count);
            });
        } catch (Exception e) {
            log.warn("Error getting posts by source: {}", e.getMessage());
        }
        return result;
    }
    
    private Map<String, Long> getPostsByStatus() {
        Map<String, Long> result = new HashMap<>();
        try {
            rawPostRepository.countByStatus().forEach(arr -> {
                String status = ((com.marketmind.domain.PostStatus) arr[0]).name();
                Long count = (Long) arr[1];
                result.put(status, count);
            });
        } catch (Exception e) {
            log.warn("Error getting posts by status: {}", e.getMessage());
        }
        return result;
    }
    
    private Map<String, Long> getOpportunitiesByStatus() {
        Map<String, Long> result = new HashMap<>();
        try {
            opportunityRepository.countByStatus().forEach(arr -> {
                String status = ((com.marketmind.domain.OpportunityStatus) arr[0]).name();
                Long count = (Long) arr[1];
                result.put(status, count);
            });
        } catch (Exception e) {
            log.warn("Error getting opportunities by status: {}", e.getMessage());
        }
        return result;
    }
}
