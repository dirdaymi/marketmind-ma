package com.marketmind.repository;

import com.marketmind.domain.PostSource;
import com.marketmind.domain.PostStatus;
import com.marketmind.domain.RawPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RawPostRepository extends JpaRepository<RawPost, Long> {
    
    Optional<RawPost> findByExternalId(String externalId);
    
    boolean existsByExternalId(String externalId);
    
    List<RawPost> findBySource(PostSource source);
    
    List<RawPost> findByStatus(PostStatus status);
    
    Page<RawPost> findBySource(PostSource source, Pageable pageable);
    
    Page<RawPost> findByStatus(PostStatus status, Pageable pageable);
    
    @Query("SELECT rp FROM RawPost rp WHERE rp.status = :status AND rp.embedding IS NULL")
    List<RawPost> findWithoutEmbedding(@Param("status") PostStatus status);
    
    @Query("SELECT rp FROM RawPost rp WHERE rp.collectedAt BETWEEN :startDate AND :endDate")
    List<RawPost> findByCollectedAtBetween(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);
    
    @Query(value = "SELECT * FROM marketmind.raw_posts rp " +
            "WHERE rp.search_vector @@ plainto_tsquery('simple', :query) " +
            "ORDER BY ts_rank(rp.search_vector, plainto_tsquery('simple', :query)) DESC",
            nativeQuery = true)
    List<RawPost> searchByText(@Param("query") String query);
    
    @Query("SELECT rp.source, COUNT(rp) FROM RawPost rp GROUP BY rp.source")
    List<Object[]> countBySource();
    
    @Query("SELECT rp.status, COUNT(rp) FROM RawPost rp GROUP BY rp.status")
    List<Object[]> countByStatus();
    
    @Query("SELECT DATE(rp.collectedAt), COUNT(rp) FROM RawPost rp " +
            "WHERE rp.collectedAt >= :since GROUP BY DATE(rp.collectedAt) ORDER BY DATE(rp.collectedAt)")
    List<Object[]> countByDateSince(@Param("since") ZonedDateTime since);
    
    long countBySource(PostSource source);
    
    long countByStatus(PostStatus status);
    
    @Query("SELECT COUNT(rp) FROM RawPost rp WHERE rp.collectedAt >= :since")
    long countSince(@Param("since") ZonedDateTime since);
}
