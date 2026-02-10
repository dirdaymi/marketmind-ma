package com.marketmind.repository;

import com.marketmind.domain.Opportunity;
import com.marketmind.domain.OpportunityPriority;
import com.marketmind.domain.OpportunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    
    List<Opportunity> findByStatus(OpportunityStatus status);
    
    Page<Opportunity> findByStatus(OpportunityStatus status, Pageable pageable);
    
    List<Opportunity> findByPriority(OpportunityPriority priority);
    
    List<Opportunity> findByClusterId(Long clusterId);
    
    @Query("SELECT o FROM Opportunity o WHERE o.overallScore >= :minScore ORDER BY o.overallScore DESC")
    List<Opportunity> findHighScoring(@Param("minScore") double minScore, Pageable pageable);
    
    @Query("SELECT o FROM Opportunity o WHERE o.status = :status AND o.priority = :priority")
    List<Opportunity> findByStatusAndPriority(
            @Param("status") OpportunityStatus status,
            @Param("priority") OpportunityPriority priority);
    
    @Query("SELECT o FROM Opportunity o WHERE :tag MEMBER OF o.tags")
    List<Opportunity> findByTag(@Param("tag") String tag);
    
    @Query("SELECT o FROM Opportunity o WHERE o.category = :category")
    List<Opportunity> findByCategory(@Param("category") String category);
    
    @Query("SELECT o.status, COUNT(o) FROM Opportunity o GROUP BY o.status")
    List<Object[]> countByStatus();
    
    @Query("SELECT o.priority, COUNT(o) FROM Opportunity o GROUP BY o.priority")
    List<Object[]> countByPriority();
    
    @Query("SELECT AVG(o.overallScore) FROM Opportunity o")
    Double averageOverallScore();
    
    @Query("SELECT AVG(o.confidenceScore) FROM Opportunity o")
    Double averageConfidenceScore();
    
    @Query("SELECT AVG(o.marketPotentialScore) FROM Opportunity o")
    Double averageMarketPotentialScore();
    
    @Query("SELECT AVG(o.feasibilityScore) FROM Opportunity o")
    Double averageFeasibilityScore();
    
    @Query("SELECT COUNT(o) FROM Opportunity o WHERE o.createdAt >= :since")
    long countSince(@Param("since") ZonedDateTime since);
    
    @Query("SELECT o FROM Opportunity o WHERE o.overallScore IS NOT NULL ORDER BY o.overallScore DESC")
    List<Opportunity> findAllOrderedByScore(Pageable pageable);
    
    @Query("SELECT o.category, COUNT(o), AVG(o.overallScore) FROM Opportunity o " +
            "WHERE o.category IS NOT NULL GROUP BY o.category")
    List<Object[]> statisticsByCategory();
}
