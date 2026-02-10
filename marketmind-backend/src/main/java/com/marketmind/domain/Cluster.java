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
@Table(name = "clusters", schema = "marketmind")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cluster {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cluster_label", nullable = false, unique = true, length = 100)
    private String clusterLabel;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "post_count")
    @Builder.Default
    private Integer postCount = 0;
    
    @Column(name = "avg_confidence")
    @Builder.Default
    private Double avgConfidence = 0.0;
    
    @Column(columnDefinition = "vector(384)")
    private float[] centroid;
    
    @ElementCollection
    @CollectionTable(name = "cluster_key_terms", schema = "marketmind",
            joinColumns = @JoinColumn(name = "cluster_id"))
    @Column(name = "term")
    private List<String> keyTerms;
    
    @Type(JsonType.class)
    @Column(name = "representative_posts", columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> representativePosts = List.of();
    
    @Type(JsonType.class)
    @Column(name = "dbscan_params", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> dbscanParams = Map.of();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @ManyToMany
    @JoinTable(
            name = "cluster_memberships",
            schema = "marketmind",
            joinColumns = @JoinColumn(name = "cluster_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<RawPost> posts;
    
    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Opportunity> opportunities;
}
