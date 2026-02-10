package com.marketmind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {
    
    private List<EmbeddingResult> embeddings;
    private String model;
    private Integer dimension;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddingResult {
        private Long postId;
        private float[] embedding;
        private Boolean success;
        private String error;
    }
}
