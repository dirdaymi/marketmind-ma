package com.marketmind.service;

import com.marketmind.domain.Cluster;
import com.marketmind.domain.Opportunity;
import com.marketmind.domain.OpportunityPriority;
import com.marketmind.domain.OpportunityStatus;
import com.marketmind.dto.*;
import com.marketmind.repository.ClusterRepository;
import com.marketmind.repository.OpportunityRepository;
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
public class OpportunityService {
    
    private final OpportunityRepository opportunityRepository;
    private final ClusterRepository clusterRepository;
    
    @Transactional(readOnly = true)
    public PageResponse<OpportunityDto> getAllOpportunities(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Opportunity> opportunities = opportunityRepository.findAll(pageable);
        return mapToPageResponse(opportunities);
    }
    
    @Transactional(readOnly = true)
    public Optional<OpportunityDto> getOpportunityById(Long id) {
        return opportunityRepository.findById(id).map(this::mapToDto);
    }
    
    @Transactional(readOnly = true)
    public List<OpportunityDto> getOpportunitiesByStatus(OpportunityStatus status) {
        return opportunityRepository.findByStatus(status)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OpportunityDto> getOpportunitiesByPriority(OpportunityPriority priority) {
        return opportunityRepository.findByPriority(priority)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OpportunityDto> getHighScoringOpportunities(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return opportunityRepository.findHighScoring(70.0, pageable)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OpportunityDto> getOpportunitiesByCluster(Long clusterId) {
        return opportunityRepository.findByClusterId(clusterId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OpportunityDto createOpportunity(CreateOpportunityRequest request, String createdBy) {
        Opportunity opportunity = new Opportunity();
        
        if (request.getClusterId() != null) {
            Cluster cluster = clusterRepository.findById(request.getClusterId())
                    .orElseThrow(() -> new RuntimeException("Cluster not found: " + request.getClusterId()));
            opportunity.setCluster(cluster);
        }
        
        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setProblemStatement(request.getProblemStatement());
        opportunity.setProposedSolution(request.getProposedSolution());
        
        // Market sizing
        opportunity.setTamSize(request.getTamSize());
        opportunity.setTamCurrency(request.getTamCurrency() != null ? request.getTamCurrency() : "MAD");
        opportunity.setSamSize(request.getSamSize());
        opportunity.setSamCurrency(request.getSamCurrency() != null ? request.getSamCurrency() : "MAD");
        opportunity.setSomSize(request.getSomSize());
        opportunity.setSomCurrency(request.getSomCurrency() != null ? request.getSomCurrency() : "MAD");
        opportunity.setMarketAssumptions(request.getMarketAssumptions());
        
        // Competition
        opportunity.setCompetitors(request.getCompetitors());
        opportunity.setCompetitiveAdvantage(request.getCompetitiveAdvantage());
        
        // Scoring
        opportunity.setConfidenceScore(request.getConfidenceScore() != null ? request.getConfidenceScore() : 0.0);
        opportunity.setMarketPotentialScore(request.getMarketPotentialScore() != null ? request.getMarketPotentialScore() : 0.0);
        opportunity.setFeasibilityScore(request.getFeasibilityScore() != null ? request.getFeasibilityScore() : 0.0);
        
        opportunity.setStatus(OpportunityStatus.DRAFT);
        opportunity.setPriority(request.getPriority() != null ? request.getPriority() : OpportunityPriority.MEDIUM);
        
        opportunity.setTags(request.getTags());
        opportunity.setCategory(request.getCategory());
        opportunity.setTargetAudience(request.getTargetAudience());
        
        opportunity.setCreatedBy(createdBy);
        
        Opportunity saved = opportunityRepository.save(opportunity);
        log.info("Created opportunity with id: {}", saved.getId());
        return mapToDto(saved);
    }
    
    @Transactional
    public OpportunityDto updateOpportunity(Long id, UpdateOpportunityRequest request) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        
        if (request.getTitle() != null) opportunity.setTitle(request.getTitle());
        if (request.getDescription() != null) opportunity.setDescription(request.getDescription());
        if (request.getProblemStatement() != null) opportunity.setProblemStatement(request.getProblemStatement());
        if (request.getProposedSolution() != null) opportunity.setProposedSolution(request.getProposedSolution());
        
        // Market sizing
        if (request.getTamSize() != null) opportunity.setTamSize(request.getTamSize());
        if (request.getTamCurrency() != null) opportunity.setTamCurrency(request.getTamCurrency());
        if (request.getSamSize() != null) opportunity.setSamSize(request.getSamSize());
        if (request.getSamCurrency() != null) opportunity.setSamCurrency(request.getSamCurrency());
        if (request.getSomSize() != null) opportunity.setSomSize(request.getSomSize());
        if (request.getSomCurrency() != null) opportunity.setSomCurrency(request.getSomCurrency());
        if (request.getMarketAssumptions() != null) opportunity.setMarketAssumptions(request.getMarketAssumptions());
        
        // Competition
        if (request.getCompetitors() != null) opportunity.setCompetitors(request.getCompetitors());
        if (request.getCompetitiveAdvantage() != null) opportunity.setCompetitiveAdvantage(request.getCompetitiveAdvantage());
        
        // Scoring
        if (request.getConfidenceScore() != null) opportunity.setConfidenceScore(request.getConfidenceScore());
        if (request.getMarketPotentialScore() != null) opportunity.setMarketPotentialScore(request.getMarketPotentialScore());
        if (request.getFeasibilityScore() != null) opportunity.setFeasibilityScore(request.getFeasibilityScore());
        
        // Status
        if (request.getStatus() != null) {
            opportunity.setStatus(request.getStatus());
            if (request.getStatus() == OpportunityStatus.VALIDATED) {
                opportunity.setValidatedAt(ZonedDateTime.now());
            }
        }
        if (request.getPriority() != null) opportunity.setPriority(request.getPriority());
        
        // Validation
        if (request.getValidationNotes() != null) opportunity.setValidationNotes(request.getValidationNotes());
        
        // Enrichment
        if (request.getTags() != null) opportunity.setTags(request.getTags());
        if (request.getCategory() != null) opportunity.setCategory(request.getCategory());
        if (request.getTargetAudience() != null) opportunity.setTargetAudience(request.getTargetAudience());
        
        Opportunity saved = opportunityRepository.save(opportunity);
        log.info("Updated opportunity with id: {}", saved.getId());
        return mapToDto(saved);
    }
    
    @Transactional
    public OpportunityDto validateOpportunity(Long id, String validatedBy, String notes) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        
        opportunity.setStatus(OpportunityStatus.VALIDATED);
        opportunity.setValidatedBy(validatedBy);
        opportunity.setValidatedAt(ZonedDateTime.now());
        opportunity.setValidationNotes(notes);
        
        Opportunity saved = opportunityRepository.save(opportunity);
        log.info("Validated opportunity with id: {} by {}", saved.getId(), validatedBy);
        return mapToDto(saved);
    }
    
    @Transactional
    public void deleteOpportunity(Long id) {
        opportunityRepository.deleteById(id);
        log.info("Deleted opportunity with id: {}", id);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Long> getOpportunitiesByStatus() {
        return opportunityRepository.countByStatus()
                .stream()
                .collect(Collectors.toMap(
                        arr -> ((OpportunityStatus) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
    }
    
    @Transactional(readOnly = true)
    public Map<String, Long> getOpportunitiesByPriority() {
        return opportunityRepository.countByPriority()
                .stream()
                .collect(Collectors.toMap(
                        arr -> ((OpportunityPriority) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
    }
    
    @Transactional(readOnly = true)
    public Double getAverageOverallScore() {
        return opportunityRepository.averageOverallScore();
    }
    
    @Transactional(readOnly = true)
    public Double getAverageConfidenceScore() {
        return opportunityRepository.averageConfidenceScore();
    }
    
    @Transactional(readOnly = true)
    public Double getAverageMarketPotentialScore() {
        return opportunityRepository.averageMarketPotentialScore();
    }
    
    @Transactional(readOnly = true)
    public Double getAverageFeasibilityScore() {
        return opportunityRepository.averageFeasibilityScore();
    }
    
    @Transactional(readOnly = true)
    public long countTotalOpportunities() {
        return opportunityRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long countOpportunitiesSince(ZonedDateTime since) {
        return opportunityRepository.countSince(since);
    }
    
    private OpportunityDto mapToDto(Opportunity opp) {
        return OpportunityDto.builder()
                .id(opp.getId())
                .clusterId(opp.getCluster() != null ? opp.getCluster().getId() : null)
                .clusterName(opp.getCluster() != null ? opp.getCluster().getName() : null)
                .title(opp.getTitle())
                .description(opp.getDescription())
                .problemStatement(opp.getProblemStatement())
                .proposedSolution(opp.getProposedSolution())
                .tamSize(opp.getTamSize())
                .tamCurrency(opp.getTamCurrency())
                .samSize(opp.getSamSize())
                .samCurrency(opp.getSamCurrency())
                .somSize(opp.getSomSize())
                .somCurrency(opp.getSomCurrency())
                .marketAssumptions(opp.getMarketAssumptions())
                .competitors(opp.getCompetitors())
                .competitiveAdvantage(opp.getCompetitiveAdvantage())
                .confidenceScore(opp.getConfidenceScore())
                .marketPotentialScore(opp.getMarketPotentialScore())
                .feasibilityScore(opp.getFeasibilityScore())
                .overallScore(opp.getOverallScore())
                .status(opp.getStatus())
                .priority(opp.getPriority())
                .validatedBy(opp.getValidatedBy())
                .validatedAt(opp.getValidatedAt())
                .validationNotes(opp.getValidationNotes())
                .tags(opp.getTags())
                .category(opp.getCategory())
                .targetAudience(opp.getTargetAudience())
                .sourcePosts(opp.getSourcePosts())
                .analysisMetadata(opp.getAnalysisMetadata())
                .createdAt(opp.getCreatedAt())
                .updatedAt(opp.getUpdatedAt())
                .createdBy(opp.getCreatedBy())
                .build();
    }
    
    private PageResponse<OpportunityDto> mapToPageResponse(Page<Opportunity> page) {
        return PageResponse.<OpportunityDto>builder()
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
