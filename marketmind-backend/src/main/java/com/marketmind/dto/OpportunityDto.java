package com.marketmind.dto;

import com.marketmind.domain.OpportunityPriority;
import com.marketmind.domain.OpportunityStatus;
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
public class OpportunityDto {
    
    private Long id;
    private Long clusterId;
    private String clusterName;
    
    private String title;
    private String description;
    private String problemStatement;
    private String proposedSolution;
    
    // Market sizing
    private Long tamSize;
    private String tamCurrency;
    private Long samSize;
    private String samCurrency;
    private Long somSize;
    private String somCurrency;
    private Map<String, Object> marketAssumptions;
    
    // Competition
    private List<Map<String, Object>> competitors;
    private String competitiveAdvantage;
    
    // Scoring
    private Double confidenceScore;
    private Double marketPotentialScore;
    private Double feasibilityScore;
    private Double overallScore;
    
    // Status
    private OpportunityStatus status;
    private OpportunityPriority priority;
    
    // Validation
    private String validatedBy;
    private ZonedDateTime validatedAt;
    private String validationNotes;
    
    // Enrichment
    private List<String> tags;
    private String category;
    private String targetAudience;
    
    // Traceability
    private List<Map<String, Object>> sourcePosts;
    private Map<String, Object> analysisMetadata;
    
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String createdBy;
}
