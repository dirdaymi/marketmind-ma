package com.marketmind.dto;

import com.marketmind.domain.OpportunityPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOpportunityRequest {
    
    private Long clusterId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
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
    
    private OpportunityPriority priority;
    private List<String> tags;
    private String category;
    private String targetAudience;
}
