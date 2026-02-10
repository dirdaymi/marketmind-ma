package com.marketmind.service;

import com.marketmind.domain.PostSource;
import com.marketmind.domain.PostStatus;
import com.marketmind.domain.RawPost;
import com.marketmind.dto.PageResponse;
import com.marketmind.dto.RawPostDto;
import com.marketmind.repository.RawPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawPostService {
    
    private final RawPostRepository rawPostRepository;
    
    @Transactional(readOnly = true)
    public PageResponse<RawPostDto> getAllPosts(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RawPost> posts = rawPostRepository.findAll(pageable);
        return mapToPageResponse(posts);
    }
    
    @Transactional(readOnly = true)
    public Optional<RawPostDto> getPostById(Long id) {
        return rawPostRepository.findById(id).map(this::mapToDto);
    }
    
    @Transactional(readOnly = true)
    public Optional<RawPostDto> getPostByExternalId(String externalId) {
        return rawPostRepository.findByExternalId(externalId).map(this::mapToDto);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<RawPostDto> getPostsBySource(PostSource source, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("collectedAt").descending());
        Page<RawPost> posts = rawPostRepository.findBySource(source, pageable);
        return mapToPageResponse(posts);
    }
    
    @Transactional(readOnly = true)
    public PageResponse<RawPostDto> getPostsByStatus(PostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("collectedAt").descending());
        Page<RawPost> posts = rawPostRepository.findByStatus(status, pageable);
        return mapToPageResponse(posts);
    }
    
    @Transactional(readOnly = true)
    public List<RawPostDto> getPostsWithoutEmbeddings() {
        return rawPostRepository.findWithoutEmbedding(PostStatus.RAW)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RawPostDto> searchPosts(String query) {
        return rawPostRepository.searchByText(query)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RawPostDto savePost(RawPostDto dto) {
        RawPost post = mapToEntity(dto);
        RawPost saved = rawPostRepository.save(post);
        return mapToDto(saved);
    }
    
    @Transactional
    public RawPostDto createPost(String externalId, PostSource source, String title, 
                                  String content, String author, String sourceUrl,
                                  ZonedDateTime postedAt, List<String> keywords,
                                  Map<String, Object> metadata, Map<String, Object> rawData) {
        
        if (rawPostRepository.existsByExternalId(externalId)) {
            log.warn("Post with externalId {} already exists, skipping", externalId);
            return null;
        }
        
        RawPost post = RawPost.builder()
                .externalId(externalId)
                .source(source)
                .title(title)
                .content(content)
                .author(author)
                .sourceUrl(sourceUrl)
                .postedAt(postedAt)
                .keywords(keywords)
                .metadata(metadata)
                .rawData(rawData)
                .status(PostStatus.RAW)
                .build();
        
        RawPost saved = rawPostRepository.save(post);
        log.info("Created new post with id: {}, externalId: {}", saved.getId(), saved.getExternalId());
        return mapToDto(saved);
    }
    
    @Transactional
    public void updatePostStatus(Long id, PostStatus status) {
        rawPostRepository.findById(id).ifPresent(post -> {
            post.setStatus(status);
            rawPostRepository.save(post);
        });
    }
    
    @Transactional(readOnly = true)
    public Map<String, Long> getPostsBySource() {
        return rawPostRepository.countBySource()
                .stream()
                .collect(Collectors.toMap(
                        arr -> ((PostSource) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
    }
    
    @Transactional(readOnly = true)
    public Map<String, Long> getPostsByStatus() {
        return rawPostRepository.countByStatus()
                .stream()
                .collect(Collectors.toMap(
                        arr -> ((PostStatus) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
    }
    
    @Transactional(readOnly = true)
    public long countTotalPosts() {
        return rawPostRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long countPostsSince(ZonedDateTime since) {
        return rawPostRepository.countSince(since);
    }
    
    private RawPostDto mapToDto(RawPost post) {
        return RawPostDto.builder()
                .id(post.getId())
                .externalId(post.getExternalId())
                .source(post.getSource())
                .sourceUrl(post.getSourceUrl())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .language(post.getLanguage())
                .postedAt(post.getPostedAt())
                .collectedAt(post.getCollectedAt())
                .status(post.getStatus())
                .keywords(post.getKeywords())
                .metadata(post.getMetadata())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
    
    private RawPost mapToEntity(RawPostDto dto) {
        return RawPost.builder()
                .id(dto.getId())
                .externalId(dto.getExternalId())
                .source(dto.getSource())
                .sourceUrl(dto.getSourceUrl())
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(dto.getAuthor())
                .language(dto.getLanguage())
                .postedAt(dto.getPostedAt())
                .status(dto.getStatus())
                .keywords(dto.getKeywords())
                .metadata(dto.getMetadata())
                .build();
    }
    
    private PageResponse<RawPostDto> mapToPageResponse(Page<RawPost> page) {
        return PageResponse.<RawPostDto>builder()
                .content(page.getContent().stream().map(this::mapToDto).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
