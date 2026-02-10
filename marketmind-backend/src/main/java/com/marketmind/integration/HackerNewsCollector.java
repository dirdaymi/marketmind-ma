package com.marketmind.integration;

import com.marketmind.domain.CollectionJob;
import com.marketmind.domain.PostSource;
import com.marketmind.repository.CollectionJobRepository;
import com.marketmind.service.RawPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class HackerNewsCollector implements DataCollector {
    
    private final RawPostService rawPostService;
    private final CollectionJobRepository collectionJobRepository;
    
    @Value("${marketmind.collectors.hackernews.enabled:true}")
    private boolean enabled;
    
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://hacker-news.firebaseio.com/v0")
            .build();
    
    // Keywords to filter for Morocco/Africa related content
    private final String[] moroccoKeywords = {
        "morocco", "maroc", "casablanca", "rabat", "marrakech", "africa", "afrique",
        "maghreb", "north africa", "mENA", "arab", "arabic"
    };
    
    @Override
    public void collect() {
        if (!enabled) {
            log.info("Hacker News collector is disabled");
            return;
        }
        
        CollectionJob job = createJob();
        int totalCollected = 0;
        int totalNew = 0;
        int totalDuplicated = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        
        try {
            // Get top stories
            List<Integer> topStoryIds = webClient.get()
                    .uri("/topstories.json")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            
            if (topStoryIds != null) {
                // Process first 50 stories
                for (Integer storyId : topStoryIds.subList(0, Math.min(50, topStoryIds.size()))) {
                    try {
                        Map<String, Object> story = webClient.get()
                                .uri("/item/{id}.json", storyId)
                                .accept(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();
                        
                        if (story != null && isMoroccoRelated(story)) {
                            processStory(story, totalNew, totalDuplicated);
                            totalCollected++;
                        }
                    } catch (Exception e) {
                        log.error("Error processing HN story {}: {}", storyId, e.getMessage());
                        errors.add(Map.of("storyId", storyId, "error", e.getMessage()));
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error collecting from Hacker News: {}", e.getMessage());
            errors.add(Map.of("error", e.getMessage()));
        }
        
        completeJob(job, totalCollected, totalNew, totalDuplicated, errors);
        log.info("Hacker News collection completed. Collected: {}, New: {}, Duplicated: {}", 
                totalCollected, totalNew, totalDuplicated);
    }
    
    private boolean isMoroccoRelated(Map<String, Object> story) {
        String title = ((String) story.getOrDefault("title", "")).toLowerCase();
        String text = "";
        if (story.containsKey("text")) {
            text = ((String) story.get("text")).toLowerCase();
        }
        
        String combined = title + " " + text;
        
        for (String keyword : moroccoKeywords) {
            if (combined.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private void processStory(Map<String, Object> story, int totalNew, int totalDuplicated) {
        String externalId = "hn_" + story.get("id");
        
        // Check for duplicates
        if (rawPostService.getPostByExternalId(externalId).isPresent()) {
            return;
        }
        
        String title = (String) story.get("title");
        String text = (String) story.getOrDefault("text", "");
        String author = (String) story.getOrDefault("by", "unknown");
        String url = "https://news.ycombinator.com/item?id=" + story.get("id");
        
        if (story.containsKey("url")) {
            url = (String) story.get("url");
        }
        
        Long time = ((Number) story.get("time")).longValue();
        ZonedDateTime postedAt = Instant.ofEpochSecond(time)
                .atZone(ZoneId.systemDefault());
        
        List<String> keywords = extractKeywords(title + " " + text);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("score", story.get("score"));
        metadata.put("descendants", story.get("descendants"));
        metadata.put("type", story.get("type"));
        
        rawPostService.createPost(
                externalId,
                PostSource.HACKER_NEWS,
                title,
                text.isEmpty() ? title : text,
                author,
                url,
                postedAt,
                keywords,
                metadata,
                story
        );
    }
    
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        String[] techKeywords = {
            "startup", "tech", "technology", "programming", "software", "hardware",
            "ai", "machine learning", "data", "cloud", "web", "mobile", "app"
        };
        
        for (String keyword : techKeywords) {
            if (lowerText.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        // Add Morocco-related keywords
        for (String keyword : moroccoKeywords) {
            if (lowerText.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
    
    private CollectionJob createJob() {
        CollectionJob job = CollectionJob.builder()
                .source(PostSource.HACKER_NEWS)
                .jobType("COLLECT")
                .status("RUNNING")
                .parameters(Map.of())
                .startedAt(ZonedDateTime.now())
                .build();
        return collectionJobRepository.save(job);
    }
    
    private void completeJob(CollectionJob job, int collected, int newPosts, int duplicated, List<Map<String, Object>> errors) {
        job.setStatus("COMPLETED");
        job.setPostsCollected(collected);
        job.setPostsNew(newPosts);
        job.setPostsDuplicated(duplicated);
        job.setErrors(errors);
        job.setCompletedAt(ZonedDateTime.now());
        collectionJobRepository.save(job);
    }
    
    @Override
    public PostSource getSource() {
        return PostSource.HACKER_NEWS;
    }
}
