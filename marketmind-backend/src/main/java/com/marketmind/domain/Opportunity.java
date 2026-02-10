package com.marketmind.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "opportunities", schema = "marketmind")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "problem_statement", columnDefinition = "TEXT")
    private String problemStatement;
    
    @Column(name = "proposed_solution", columnDefinition = "TEXT")
    private String proposedSolution;
    
    // TAM (Total Addressable Market)
    @Column(name = "tam_size")
    private Long tamSize;
    
    @Column(name = "tam_currency", length = 3)
    @Builder.Default
    private String tamCurrency = "MAD";
    
    // SAM (Serviceable Addressable Market)
    @Column(name = "sam_size")
    private Long samSize;
    
    @Column(name = "sam_currency", length = 3)
    @Builder.Default
    private String samCurrency = "MAD";
    
    // SOM (Serviceable Obtainable Market)
    @Column(name = "som_size")
    private Long somSize;
    
    @Column(name = "som_currency", length = 3)
    @Builder.Default
    private String somCurrency = "MAD";
    
    @Type(JsonType.class)
    @Column(name = "market_assumptions", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> marketAssumptions = Map.of();
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> competitors = List.of();
    
    @Column(name = "competitive_advantage", columnDefinition = "TEXT")
    private String competitiveAdvantage;
    
    @Column(name = "confidence_score")
    @Builder.Default
    private Double confidenceScore = 0.0;
    
    @Column(name = "market_potential_score")
    @Builder.Default
    private Double marketPotentialScore = 0.0;
    
    @Column(name = "feasibility_score")
    @Builder.Default
    private Double feasibilityScore = 0.0;
    
    @Column(name = "overall_score", insertable = false, updatable = false)
    private Double overallScore;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OpportunityStatus status = OpportunityStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OpportunityPriority priority = OpportunityPriority.MEDIUM;
    
    @Column(name = "validated_by")
    private String validatedBy;
    
    @Column(name = "validated_at")
    private ZonedDateTime validatedAt;
    
    @Column(name = "validation_notes", columnDefinition = "TEXT")
    private String validationNotes;
    
    @ElementCollection
    @CollectionTable(name = "opportunity_tags", schema = "marketmind",
            joinColumns = @JoinColumn(name = "opportunity_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    @Column(length = 100)
    private String category;
    
    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;
    
    @Type(JsonType.class)
    @Column(name = "source_posts", columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> sourcePosts = List.of();
    
    @Type(JsonType.class)
    @Column(name = "analysis_metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> analysisMetadata = Map.of();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    @Column(name = "created_by")
    @Builder.Default
    private String createdBy = "system";
}
