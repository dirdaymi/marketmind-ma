package com.marketmind.repository;

import com.marketmind.domain.CollectionJob;
import com.marketmind.domain.PostSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface CollectionJobRepository extends JpaRepository<CollectionJob, Long> {
    
    List<CollectionJob> findBySource(PostSource source);
    
    List<CollectionJob> findByStatus(String status);
    
    @Query("SELECT cj FROM CollectionJob cj WHERE cj.createdAt >= :since ORDER BY cj.createdAt DESC")
    List<CollectionJob> findRecent(@Param("since") ZonedDateTime since);
    
    @Query("SELECT cj.source, COUNT(cj), SUM(cj.postsCollected), SUM(cj.postsNew) " +
            "FROM CollectionJob cj WHERE cj.status = 'COMPLETED' GROUP BY cj.source")
    List<Object[]> statisticsBySource();
    
    @Query("SELECT SUM(cj.postsCollected) FROM CollectionJob cj")
    Long totalPostsCollected();
    
    @Query("SELECT SUM(cj.postsNew) FROM CollectionJob cj")
    Long totalPostsNew();
}
