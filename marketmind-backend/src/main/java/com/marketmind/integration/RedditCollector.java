package com.marketmind.integration;

import com.marketmind.domain.CollectionJob;
import com.marketmind.domain.PostSource;
import com.marketmind.repository.CollectionJobRepository;
import com.marketmind.service.RawPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
public class RedditCollector implements DataCollector {
    
    private final RawPostService rawPostService;
    private final CollectionJobRepository collectionJobRepository;
    
    @Value("${marketmind.collectors.reddit.enabled:true}")
    private boolean enabled;
    
    @Value("${marketmind.collectors.reddit.subreddits:morocco,Casablanca,MoroccoTech,startups}")
    private String subreddits;
    
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://www.reddit.com")
            .defaultHeader(HttpHeaders.USER_AGENT, "MarketMindMA/1.0")
            .build();
    
    @Override
    public void collect() {
        if (!enabled) {
            log.info("Reddit collector is disabled");
            return;
        }
        
        CollectionJob job = createJob();
        int totalCollected = 0;
        int totalNew = 0;
        int totalDuplicated = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        
        String[] subredditList = subreddits.split(",");
        
        for (String subreddit : subredditList) {
            subreddit = subreddit.trim();
            try {
                log.info("Collecting from r/{}...", subreddit);
                
                Map<String, Object> response = webClient.get()
                        .uri("/r/{subreddit}/new.json?limit=25", subreddit)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                
                if (response != null && response.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children");
                    
                    for (Map<String, Object> child : children) {
                        Map<String, Object> post = (Map<String, Object>) child.get("data");
                        
                        try {
                            String externalId = "reddit_" + post.get("id");
                            
                            // Check for duplicates
                            if (rawPostService.getPostByExternalId(externalId).isPresent()) {
                                totalDuplicated++;
                                continue;
                            }
                            
                            String title = (String) post.getOrDefault("title", "");
                            String selftext = (String) post.getOrDefault("selftext", "");
                            String content = selftext.isEmpty() ? title : title + "\n\n" + selftext;
                            String author = (String) post.getOrDefault("author", "unknown");
                            String permalink = (String) post.get("permalink");
                            String url = "https://reddit.com" + permalink;
                            
                            // Parse created_utc
                            Double createdUtc = ((Number) post.get("created_utc")).doubleValue();
                            ZonedDateTime postedAt = Instant.ofEpochSecond(createdUtc.longValue())
                                    .atZone(ZoneId.systemDefault());
                            
                            // Extract keywords
                            List<String> keywords = extractKeywords(title + " " + content);
                            
                            // Create metadata
                            Map<String, Object> metadata = new HashMap<>();
                            metadata.put("subreddit", subreddit);
                            metadata.put("score", post.get("score"));
                            metadata.put("num_comments", post.get("num_comments"));
                            metadata.put("upvote_ratio", post.get("upvote_ratio"));
                            
                            rawPostService.createPost(
                                    externalId,
                                    PostSource.REDDIT,
                                    title,
                                    content,
                                    author,
                                    url,
                                    postedAt,
                                    keywords,
                                    metadata,
                                    post
                            );
                            
                            totalNew++;
                        } catch (Exception e) {
                            log.error("Error processing post: {}", e.getMessage());
                            errors.add(Map.of("subreddit", subreddit, "error", e.getMessage()));
                        }
                    }
                    
                    totalCollected += children.size();
                }
                
            } catch (Exception e) {
                log.error("Error collecting from r/{}: {}", subreddit, e.getMessage());
                errors.add(Map.of("subreddit", subreddit, "error", e.getMessage()));
            }
        }
        
        completeJob(job, totalCollected, totalNew, totalDuplicated, errors);
        log.info("Reddit collection completed. Collected: {}, New: {}, Duplicated: {}", 
                totalCollected, totalNew, totalDuplicated);
    }
    
    private CollectionJob createJob() {
        CollectionJob job = CollectionJob.builder()
                .source(PostSource.REDDIT)
                .jobType("COLLECT")
                .status("RUNNING")
                .parameters(Map.of("subreddits", subreddits))
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
    
    private List<String> extractKeywords(String text) {
        // Simple keyword extraction - in production, use NLP
        List<String> keywords = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        String[] moroccoKeywords = {
            "morocco", "maroc", "casablanca", "rabat", "marrakech", "tanger", "agadir", "fes",
            "startup", "business", "entrepreneur", "tech", "technology", "digital",
            "problem", "issue", "challenge", "solution", "need", "want", "looking for"
        };
        
        for (String keyword : moroccoKeywords) {
            if (lowerText.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
    
    @Override
    public PostSource getSource() {
        return PostSource.REDDIT;
    }
}
