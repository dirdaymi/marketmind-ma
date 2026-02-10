package com.marketmind.integration;

import com.marketmind.domain.CollectionJob;
import com.marketmind.domain.PostSource;
import com.marketmind.repository.CollectionJobRepository;
import com.marketmind.service.RawPostService;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RssFeedCollector implements DataCollector {
    
    private final RawPostService rawPostService;
    private final CollectionJobRepository collectionJobRepository;
    
    @Value("${marketmind.collectors.rss.enabled:true}")
    private boolean enabled;
    
    // Default RSS feeds for Moroccan tech/startup news
    private final List<String> defaultFeeds = Arrays.asList(
            // Add actual Moroccan tech RSS feeds here
            "https://feeds.feedburner.com/techcrunch/startups",
            "https://www.wamda.com/feed"
    );
    
    @Override
    public void collect() {
        if (!enabled) {
            log.info("RSS collector is disabled");
            return;
        }
        
        CollectionJob job = createJob();
        int totalCollected = 0;
        int totalNew = 0;
        int totalDuplicated = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        
        for (String feedUrl : defaultFeeds) {
            try {
                log.info("Collecting from RSS feed: {}", feedUrl);
                
                URL url = new URL(feedUrl);
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(url));
                
                for (SyndEntry entry : feed.getEntries()) {
                    try {
                        String externalId = "rss_" + Math.abs(entry.getUri().hashCode());
                        
                        // Check for duplicates
                        if (rawPostService.getPostByExternalId(externalId).isPresent()) {
                            totalDuplicated++;
                            continue;
                        }
                        
                        String title = entry.getTitle();
                        String content = entry.getDescription() != null ? 
                                entry.getDescription().getValue() : title;
                        String author = entry.getAuthor() != null ? entry.getAuthor() : "unknown";
                        String link = entry.getLink();
                        
                        ZonedDateTime postedAt = entry.getPublishedDate() != null ?
                                entry.getPublishedDate().toInstant()
                                        .atZone(ZoneId.systemDefault()) :
                                ZonedDateTime.now();
                        
                        // Filter for Morocco-related content
                        String textToCheck = (title + " " + content).toLowerCase();
                        if (!isMoroccoRelated(textToCheck)) {
                            continue;
                        }
                        
                        List<String> keywords = extractKeywords(textToCheck);
                        
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("feed_url", feedUrl);
                        metadata.put("feed_title", feed.getTitle());
                        
                        rawPostService.createPost(
                                externalId,
                                PostSource.RSS_FEED,
                                title,
                                content,
                                author,
                                link,
                                postedAt,
                                keywords,
                                metadata,
                                Map.of("entry", entry.getUri())
                        );
                        
                        totalNew++;
                    } catch (Exception e) {
                        log.error("Error processing RSS entry: {}", e.getMessage());
                        errors.add(Map.of("feed", feedUrl, "error", e.getMessage()));
                    }
                }
                
                totalCollected += feed.getEntries().size();
                
            } catch (Exception e) {
                log.error("Error collecting from RSS feed {}: {}", feedUrl, e.getMessage());
                errors.add(Map.of("feed", feedUrl, "error", e.getMessage()));
            }
        }
        
        completeJob(job, totalCollected, totalNew, totalDuplicated, errors);
        log.info("RSS collection completed. Collected: {}, New: {}, Duplicated: {}", 
                totalCollected, totalNew, totalDuplicated);
    }
    
    private boolean isMoroccoRelated(String text) {
        String[] moroccoTerms = {
            "morocco", "maroc", "casablanca", "rabat", "marrakech", "tanger", "agadir", "fes",
            "meknes", "oujda", "kenitra", "tetouan", "safi", "mohammedia", "el jadida",
            "maghreb", "north africa", "afrique du nord"
        };
        
        for (String term : moroccoTerms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
    
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String[] techKeywords = {
            "startup", "tech", "technology", "digital", "innovation", "entrepreneur",
            "business", "funding", "investment", "venture", "saas", "app", "mobile"
        };
        
        for (String keyword : techKeywords) {
            if (text.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
    
    private CollectionJob createJob() {
        CollectionJob job = CollectionJob.builder()
                .source(PostSource.RSS_FEED)
                .jobType("COLLECT")
                .status("RUNNING")
                .parameters(Map.of("feeds", defaultFeeds))
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
        return PostSource.RSS_FEED;
    }
}
