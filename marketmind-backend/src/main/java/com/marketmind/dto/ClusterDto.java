package com.marketmind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterDto {
    
    private Long id;
    private String clusterLabel;
    private String name;
    private String description;
    private Integer postCount;
    private Double avgConfidence;
    private List<String> keyTerms;
    private List<Map<String, Object>> representativePosts;
    private Map<String, Object> dbscanParams;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean isActive;
    private Integer opportunityCount;
    private Double maxOpportunityScore;
}
