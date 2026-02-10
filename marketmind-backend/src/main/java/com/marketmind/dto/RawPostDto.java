package com.marketmind.dto;

import com.marketmind.domain.PostSource;
import com.marketmind.domain.PostStatus;
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
public class RawPostDto {
    
    private Long id;
    private String externalId;
    private PostSource source;
    private String sourceUrl;
    private String title;
    private String content;
    private String author;
    private String language;
    private ZonedDateTime postedAt;
    private ZonedDateTime collectedAt;
    private PostStatus status;
    private List<String> keywords;
    private Map<String, Object> metadata;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
