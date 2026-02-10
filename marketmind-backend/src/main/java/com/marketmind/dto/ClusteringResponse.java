package com.marketmind.dto;

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
public class ClusteringResponse {
    
    private List<ClusterResult> clusters;
    private Integer totalPosts;
    private Integer clusteredPosts;
    private Integer noisePoints;
    private Map<String, Object> parameters;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterResult {
        private String label;
        private List<Long> postIds;
        private float[] centroid;
        private List<String> keyTerms;
        private Integer size;
        private Double avgConfidence;
    }
}
