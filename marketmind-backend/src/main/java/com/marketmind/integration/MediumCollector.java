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

import java.time.ZonedDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediumCollector implements DataCollector {
    
    private final RawPostService rawPostService;
    private final CollectionJobRepository collectionJobRepository;
    
    @Value("${marketmind.collectors.medium.enabled:true}")
    private boolean enabled;
    
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://medium.com")
            .build();
    
    // Keywords to filter for Morocco/startup related content
    private final String[] targetKeywords = {
        "morocco", "maroc", "casablanca", "rabat", "startup", "entrepreneur",
        "tech", "technology", "africa", "maghreb", "innovation"
    };
    
    @Override
    public void collect() {
        if (!enabled) {
            log.info("Medium collector is disabled");
            return;
        }
        
        CollectionJob job = createJob();
        int totalCollected = 0;
        int totalNew = 0;
        int totalDuplicated = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        
        // Note: Medium's API requires authentication for most endpoints
        // This is a simplified implementation that would need Medium API credentials
        // For now, we'll log that collection would happen here
        
        log.info("Medium collection would run here with proper API credentials");
        
        // In a real implementation, you would:
        // 1. Use Medium's API or RSS feeds
        // 2. Search for posts with Morocco/startup tags
        // 3. Process and store relevant posts
        
        completeJob(job, totalCollected, totalNew, totalDuplicated, errors);
        log.info("Medium collection completed. Collected: {}, New: {}, Duplicated: {}", 
                totalCollected, totalNew, totalDuplicated);
    }
    
    private boolean isRelevantContent(String title, String content) {
        String combined = (title + " " + content).toLowerCase();
        for (String keyword : targetKeywords) {
            if (combined.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        for (String keyword : targetKeywords) {
            if (lowerText.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
    
    private CollectionJob createJob() {
        CollectionJob job = CollectionJob.builder()
                .source(PostSource.MEDIUM)
                .jobType("COLLECT")
                .status("RUNNING")
                .parameters(Map.of("note", "Medium API requires authentication"))
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
        return PostSource.MEDIUM;
    }
}
