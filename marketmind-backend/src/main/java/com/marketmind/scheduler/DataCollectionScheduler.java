package com.marketmind.scheduler;

import com.marketmind.integration.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataCollectionScheduler {
    
    private final RedditCollector redditCollector;
    private final RssFeedCollector rssFeedCollector;
    private final HackerNewsCollector hackerNewsCollector;
    private final MediumCollector mediumCollector;
    
    // Run every 6 hours
    @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledRedditCollection() {
        log.info("Starting scheduled Reddit collection...");
        try {
            redditCollector.collect();
        } catch (Exception e) {
            log.error("Error during Reddit collection: {}", e.getMessage(), e);
        }
    }
    
    // Run every 4 hours
    @Scheduled(cron = "0 0 */4 * * *")
    public void scheduledRssCollection() {
        log.info("Starting scheduled RSS feed collection...");
        try {
            rssFeedCollector.collect();
        } catch (Exception e) {
            log.error("Error during RSS collection: {}", e.getMessage(), e);
        }
    }
    
    // Run every 8 hours
    @Scheduled(cron = "0 0 */8 * * *")
    public void scheduledHackerNewsCollection() {
        log.info("Starting scheduled Hacker News collection...");
        try {
            hackerNewsCollector.collect();
        } catch (Exception e) {
            log.error("Error during Hacker News collection: {}", e.getMessage(), e);
        }
    }
    
    // Run every 12 hours
    @Scheduled(cron = "0 0 */12 * * *")
    public void scheduledMediumCollection() {
        log.info("Starting scheduled Medium collection...");
        try {
            mediumCollector.collect();
        } catch (Exception e) {
            log.error("Error during Medium collection: {}", e.getMessage(), e);
        }
    }
    
    // Run daily at 2 AM for full processing
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledFullProcessing() {
        log.info("Starting scheduled full processing...");
        // This would trigger embedding generation and clustering
    }
}
