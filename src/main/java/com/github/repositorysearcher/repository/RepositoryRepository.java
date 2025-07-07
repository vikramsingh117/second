package com.github.repositorysearcher.repository;

import com.github.repositorysearcher.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    Optional<Repository> findByRepositoryId(Long repositoryId);

    @Query(value = "SELECT * FROM repositories r WHERE " +
            "(:language IS NULL OR LOWER(r.programming_language) = LOWER(:language)) AND " +
            "(:minStars IS NULL OR r.stars_count >= :minStars) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'stars' THEN r.stars_count END DESC, " +
            "CASE WHEN :sort = 'forks' THEN r.forks_count END DESC, " +
            "CASE WHEN :sort = 'updated' THEN r.last_updated_date END DESC", 
            nativeQuery = true)
    List<Repository> findRepositoriesWithFilters(
            @Param("language") String language,
            @Param("minStars") Integer minStars,
            @Param("sort") String sort
    );

    @Query(value = "SELECT * FROM repositories r WHERE " +
            "LOWER(r.programming_language) = LOWER(:language) " +
            "ORDER BY r.stars_count DESC", 
            nativeQuery = true)
    List<Repository> findByProgrammingLanguageIgnoreCaseOrderByStarsCountDesc(@Param("language") String language);

    @Query(value = "SELECT * FROM repositories r WHERE " +
            "r.stars_count >= :minStars " +
            "ORDER BY r.stars_count DESC", 
            nativeQuery = true)
    List<Repository> findByStarsCountGreaterThanEqualOrderByStarsCountDesc(@Param("minStars") Integer minStars);

    List<Repository> findAllByOrderByStarsCountDesc();
    List<Repository> findAllByOrderByForksCountDesc();
    List<Repository> findAllByOrderByLastUpdatedDateDesc();
} 