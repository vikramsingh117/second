package com.github.repositorysearcher.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "repositories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"createdAt", "updatedAt"})
public class Repository {

    @Id
    @Column(name = "repository_id", unique = true, nullable = false)
    private Long repositoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "programming_language")
    private String programmingLanguage;

    @Column(name = "stars_count", nullable = false)
    private Integer starsCount;

    @Column(name = "forks_count", nullable = false)
    private Integer forksCount;

    @Column(name = "last_updated_date", nullable = false)
    private LocalDateTime lastUpdatedDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Repository(Long repositoryId, String name, String description, String ownerName,
                     String programmingLanguage, Integer starsCount, Integer forksCount,
                     LocalDateTime lastUpdatedDate) {
        this();
        this.repositoryId = repositoryId;
        this.name = name;
        this.description = description;
        this.ownerName = ownerName;
        this.programmingLanguage = programmingLanguage;
        this.starsCount = starsCount;
        this.forksCount = forksCount;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 