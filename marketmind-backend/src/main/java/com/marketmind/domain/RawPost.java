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
@Table(name = "raw_posts", schema = "marketmind")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostSource source;
    
    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;
    
    @Column(columnDefinition = "TEXT")
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    private String author;
    
    @Column(length = 10)
    private String language;
    
    @Column(name = "posted_at")
    private ZonedDateTime postedAt;
    
    @Column(name = "collected_at")
    @CreationTimestamp
    private ZonedDateTime collectedAt;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.RAW;
    
    @ElementCollection
    @CollectionTable(name = "raw_post_keywords", schema = "marketmind", 
            joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "keyword")
    private List<String> keywords;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = Map.of();
    
    @Type(JsonType.class)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> rawData = Map.of();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Embedding embedding;
    
    @ManyToMany(mappedBy = "posts")
    private List<Cluster> clusters;
}
