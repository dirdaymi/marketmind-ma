package com.marketmind.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "collection_jobs", schema = "marketmind")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostSource source;
    
    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;
    
    @Column(length = 50)
    @Builder.Default
    private String status = "PENDING";
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> parameters = Map.of();
    
    @Column(name = "posts_collected")
    @Builder.Default
    private Integer postsCollected = 0;
    
    @Column(name = "posts_new")
    @Builder.Default
    private Integer postsNew = 0;
    
    @Column(name = "posts_duplicated")
    @Builder.Default
    private Integer postsDuplicated = 0;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> errors = List.of();
    
    @Column(name = "started_at")
    private ZonedDateTime startedAt;
    
    @Column(name = "completed_at")
    private ZonedDateTime completedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
