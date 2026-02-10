package com.marketmind.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "embeddings", schema = "marketmind")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Embedding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private RawPost post;
    
    @Column(nullable = false, columnDefinition = "vector(384)")
    private float[] embedding;
    
    @Column(name = "model_name", length = 100)
    @Builder.Default
    private String modelName = "sentence-transformers/all-MiniLM-L6-v2";
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
