package com.github.repositorysearcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class GitHubApiResponse {

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("incomplete_results")
    private Boolean incompleteResults;

    private List<GitHubRepository> items;

    @Data
    @NoArgsConstructor
    public static class GitHubRepository {
        private Long id;
        private String name;
        private String description;
        private String language;

        @JsonProperty("stargazers_count")
        private Integer stargazersCount;

        @JsonProperty("forks_count")
        private Integer forksCount;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        private Owner owner;

        @Data
        @NoArgsConstructor
        public static class Owner {
            private String login;
        }
    }
} 