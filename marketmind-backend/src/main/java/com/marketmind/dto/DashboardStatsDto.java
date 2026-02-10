package com.marketmind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    
    // Post statistics
    private Long totalPosts;
    private Long postsLast24Hours;
    private Long postsLast7Days;
    private Long postsLast30Days;
    private Map<String, Long> postsBySource;
    private Map<String, Long> postsByStatus;
    
    // Cluster statistics
    private Long totalClusters;
    private Long activeClusters;
    private Double averagePostsPerCluster;
    
    // Opportunity statistics
    private Long totalOpportunities;
    private Long opportunitiesByStatusDraft;
    private Long opportunitiesByStatusValidated;
    private Long opportunitiesByStatusInProgress;
    private Long opportunitiesByStatusImplemented;
    private Double averageOpportunityScore;
    private Double averageConfidenceScore;
    private Double averageMarketPotentialScore;
    private Double averageFeasibilityScore;
    
    // Collection statistics
    private Long totalCollectionJobs;
    private Long totalPostsCollected;
    private Long totalPostsNew;
}
