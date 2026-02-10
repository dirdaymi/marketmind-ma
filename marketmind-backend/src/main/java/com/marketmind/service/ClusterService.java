package com.marketmind.service;

import com.marketmind.domain.Cluster;
import com.marketmind.domain.RawPost;
import com.marketmind.dto.ClusterDto;
import com.marketmind.dto.ClusteringRequest;
import com.marketmind.dto.ClusteringResponse;
import com.marketmind.dto.PageResponse;
import com.marketmind.repository.ClusterRepository;
import com.marketmind.repository.RawPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterService {
    
    private final ClusterRepository clusterRepository;
    private final RawPostRepository rawPostRepository;
    private final WebClient nlpWebClient;
    
    @Transactional(readOnly = true)
    public PageResponse<ClusterDto> getAllClusters(int page, int size, boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("postCount").descending());
        Page<Cluster> clusters = activeOnly 
                ? clusterRepository.findByIsActiveTrue(pageable)
                : clusterRepository.findAll(pageable);
        return mapToPageResponse(clusters);
    }
    
    @Transactional(readOnly = true)
    public Optional<ClusterDto> getClusterById(Long id) {
        return clusterRepository.findById(id).map(this::mapToDto);
    }
    
    @Transactional(readOnly = true)
    public Optional<ClusterDto> getClusterByLabel(String label) {
        return clusterRepository.findByClusterLabel(label).map(this::mapToDto);
    }
    
    @Transactional(readOnly = true)
    public List<ClusterDto> getTopClusters(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return clusterRepository.findTopClustersByPostCount(5, pageable)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ClusterDto> getClustersByTerm(String term) {
        return clusterRepository.findByKeyTerm(term)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ClusterDto createCluster(ClusterDto dto) {
        Cluster cluster = mapToEntity(dto);
        Cluster saved = clusterRepository.save(cluster);
        return mapToDto(saved);
    }
    
    @Transactional
    public ClusterDto updateCluster(Long id, ClusterDto dto) {
        return clusterRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            existing.setKeyTerms(dto.getKeyTerms());
            existing.setIsActive(dto.getIsActive());
            existing.setRepresentativePosts(dto.getRepresentativePosts());
            Cluster saved = clusterRepository.save(existing);
            return mapToDto(saved);
        }).orElseThrow(() -> new RuntimeException("Cluster not found: " + id));
    }
    
    @Transactional
    public void deleteCluster(Long id) {
        clusterRepository.findById(id).ifPresent(cluster -> {
            cluster.setIsActive(false);
            clusterRepository.save(cluster);
        });
    }
    
    @Transactional
    public void performClustering() {
        log.info("Starting clustering process...");
        
        // Get all posts with embeddings
        List<RawPost> postsWithEmbeddings = rawPostRepository.findAll()
                .stream()
                .filter(p -> p.getEmbedding() != null)
                .collect(Collectors.toList());
        
        if (postsWithEmbeddings.size() < 10) {
            log.warn("Not enough posts with embeddings for clustering. Found: {}", postsWithEmbeddings.size());
            return;
        }
        
        // Prepare request for NLP service
        List<ClusteringRequest.EmbeddingData> embeddingData = postsWithEmbeddings.stream()
                .map(p -> ClusteringRequest.EmbeddingData.builder()
                        .postId(p.getId())
                        .embedding(p.getEmbedding().getEmbedding())
                        .build())
                .collect(Collectors.toList());
        
        ClusteringRequest request = ClusteringRequest.builder()
                .embeddings(embeddingData)
                .eps(0.5)
                .minSamples(5)
                .build();
        
        // Call NLP service
        try {
            ClusteringResponse response = nlpWebClient.post()
                    .uri("/api/v1/nlp/cluster")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClusteringResponse.class)
                    .block();
            
            if (response != null && response.getClusters() != null) {
                saveClusteringResults(response, postsWithEmbeddings);
            }
        } catch (Exception e) {
            log.error("Error during clustering: {}", e.getMessage(), e);
        }
    }
    
    @Transactional
    protected void saveClusteringResults(ClusteringResponse response, List<RawPost> allPosts) {
        Map<Long, RawPost> postMap = allPosts.stream()
                .collect(Collectors.toMap(RawPost::getId, p -> p));
        
        int clusterNumber = 1;
        for (ClusteringResponse.ClusterResult result : response.getClusters()) {
            String label = "CLUSTER_" + clusterNumber++;
            
            // Check if cluster already exists
            Cluster cluster = clusterRepository.findByClusterLabel(label)
                    .orElseGet(() -> Cluster.builder().clusterLabel(label).build());
            
            cluster.setName(generateClusterName(result.getKeyTerms()));
            cluster.setDescription(generateClusterDescription(result.getKeyTerms()));
            cluster.setPostCount(result.getSize());
            cluster.setAvgConfidence(result.getAvgConfidence());
            cluster.setCentroid(result.getCentroid());
            cluster.setKeyTerms(result.getKeyTerms());
            cluster.setIsActive(true);
            
            // Get posts in this cluster
            List<RawPost> clusterPosts = result.getPostIds().stream()
                    .map(postMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            cluster.setPosts(clusterPosts);
            
            // Set representative posts
            List<Map<String, Object>> repPosts = clusterPosts.stream()
                    .limit(3)
                    .map(p -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", p.getId());
                        map.put("title", p.getTitle());
                        map.put("content", p.getContent().substring(0, Math.min(200, p.getContent().length())));
                        map.put("source", p.getSource().name());
                        return map;
                    })
                    .collect(Collectors.toList());
            cluster.setRepresentativePosts(repPosts);
            
            clusterRepository.save(cluster);
            
            // Update post statuses
            for (RawPost post : clusterPosts) {
                post.setStatus(com.marketmind.domain.PostStatus.CLUSTERED);
                rawPostRepository.save(post);
            }
        }
        
        log.info("Clustering completed. Created/updated {} clusters", response.getClusters().size());
    }
    
    private String generateClusterName(List<String> keyTerms) {
        if (keyTerms == null || keyTerms.isEmpty()) {
            return "Unnamed Cluster";
        }
        return keyTerms.stream()
                .limit(3)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }
    
    private String generateClusterDescription(List<String> keyTerms) {
        if (keyTerms == null || keyTerms.isEmpty()) {
            return "No description available";
        }
        return "Cluster related to: " + String.join(", ", keyTerms);
    }
    
    @Transactional(readOnly = true)
    public long countActiveClusters() {
        return clusterRepository.countActive();
    }
    
    @Transactional(readOnly = true)
    public Double getAveragePostsPerCluster() {
        return clusterRepository.averagePostCount();
    }
    
    private ClusterDto mapToDto(Cluster cluster) {
        Integer opportunityCount = cluster.getOpportunities() != null ? cluster.getOpportunities().size() : 0;
        Double maxScore = cluster.getOpportunities() != null 
                ? cluster.getOpportunities().stream()
                        .mapToDouble(o -> o.getOverallScore() != null ? o.getOverallScore() : 0)
                        .max()
                        .orElse(0.0)
                : 0.0;
        
        return ClusterDto.builder()
                .id(cluster.getId())
                .clusterLabel(cluster.getClusterLabel())
                .name(cluster.getName())
                .description(cluster.getDescription())
                .postCount(cluster.getPostCount())
                .avgConfidence(cluster.getAvgConfidence())
                .keyTerms(cluster.getKeyTerms())
                .representativePosts(cluster.getRepresentativePosts())
                .dbscanParams(cluster.getDbscanParams())
                .createdAt(cluster.getCreatedAt())
                .updatedAt(cluster.getUpdatedAt())
                .isActive(cluster.getIsActive())
                .opportunityCount(opportunityCount)
                .maxOpportunityScore(maxScore)
                .build();
    }
    
    private Cluster mapToEntity(ClusterDto dto) {
        return Cluster.builder()
                .id(dto.getId())
                .clusterLabel(dto.getClusterLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .keyTerms(dto.getKeyTerms())
                .isActive(dto.getIsActive())
                .representativePosts(dto.getRepresentativePosts())
                .build();
    }
    
    private PageResponse<ClusterDto> mapToPageResponse(Page<Cluster> page) {
        return PageResponse.<ClusterDto>builder()
                .content(page.getContent().stream().map(this::mapToDto).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.isFirst())
                .build();
    }
}
