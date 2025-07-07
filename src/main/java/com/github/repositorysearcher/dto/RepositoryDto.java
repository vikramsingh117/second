package com.github.repositorysearcher.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.repositorysearcher.entity.Repository;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class RepositoryDto {

    private Long id;
    private String name;
    private String description;
    private String owner;
    private String language;
    private Integer stars;
    private Integer forks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastUpdated;

    public RepositoryDto(Repository repository) {
        this.id = repository.getRepositoryId();
        this.name = repository.getName();
        this.description = repository.getDescription();
        this.owner = repository.getOwnerName();
        this.language = repository.getProgrammingLanguage();
        this.stars = repository.getStarsCount();
        this.forks = repository.getForksCount();
        this.lastUpdated = repository.getLastUpdatedDate();
    }

    public RepositoryDto(Long id, String name, String description, String owner,
                        String language, Integer stars, Integer forks, LocalDateTime lastUpdated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.lastUpdated = lastUpdated;
    }
} 