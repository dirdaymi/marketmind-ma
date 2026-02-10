package com.marketmind.repository;

import com.marketmind.domain.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmbeddingRepository extends JpaRepository<Embedding, Long> {
    
    Optional<Embedding> findByPostId(Long postId);
    
    boolean existsByPostId(Long postId);
    
    @Query(value = "SELECT e.* FROM marketmind.embeddings e " +
            "ORDER BY e.embedding <=> CAST(:queryVector AS vector) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Embedding> findNearestNeighbors(
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);
    
    @Query(value = "SELECT e.*, e.embedding <=> CAST(:queryVector AS vector) as distance " +
            "FROM marketmind.embeddings e " +
            "WHERE e.embedding <=> CAST(:queryVector AS vector) < :maxDistance " +
            "ORDER BY e.embedding <=> CAST(:queryVector AS vector) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Embedding> findSimilarEmbeddings(
            @Param("queryVector") String queryVector,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);
    
    @Query(value = "SELECT e.* FROM marketmind.embeddings e " +
            "WHERE e.post_id IN :postIds",
            nativeQuery = true)
    List<Embedding> findByPostIds(@Param("postIds") List<Long> postIds);
    
    long count();
}
