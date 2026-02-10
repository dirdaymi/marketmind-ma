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
public class EmbeddingRequest {
    
    private List<TextInput> texts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextInput {
        private Long postId;
        private String text;
    }
}
