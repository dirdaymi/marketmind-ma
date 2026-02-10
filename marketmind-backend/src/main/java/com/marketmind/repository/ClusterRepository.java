package com.marketmind.repository;

import com.marketmind.domain.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Long> {
    
    Optional<Cluster> findByClusterLabel(String clusterLabel);
    
    boolean existsByClusterLabel(String clusterLabel);
    
    List<Cluster> findByIsActiveTrue();
    
    Page<Cluster> findByIsActiveTrue(Pageable pageable);
    
    List<Cluster> findByPostCountGreaterThan(Integer minPosts);
    
    @Query("SELECT c FROM Cluster c WHERE c.postCount >= :minPosts AND c.isActive = true ORDER BY c.postCount DESC")
    List<Cluster> findTopClustersByPostCount(@Param("minPosts") int minPosts, Pageable pageable);
    
    @Query("SELECT c FROM Cluster c WHERE SIZE(c.keyTerms) > 0 AND :term MEMBER OF c.keyTerms")
    List<Cluster> findByKeyTerm(@Param("term") String term);
    
    @Query("SELECT c, COUNT(o) as oppCount FROM Cluster c LEFT JOIN c.opportunities o " +
            "GROUP BY c ORDER BY oppCount DESC")
    List<Object[]> findClustersWithOpportunityCount();
    
    @Query("SELECT COUNT(c) FROM Cluster c WHERE c.isActive = true")
    long countActive();
    
    @Query("SELECT AVG(c.postCount) FROM Cluster c WHERE c.isActive = true")
    Double averagePostCount();
    
    @Query(value = "SELECT c.* FROM marketmind.clusters c " +
            "ORDER BY c.centroid <=> CAST(:queryVector AS vector) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Cluster> findByCentroidSimilarity(
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);
}
