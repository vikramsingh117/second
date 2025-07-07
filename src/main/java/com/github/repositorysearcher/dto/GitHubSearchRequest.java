package com.github.repositorysearcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GitHubSearchRequest {

    @NotBlank(message = "Query is required")
    private String query;

    private String language;

    @Pattern(regexp = "^(stars|forks|updated)$", message = "Sort must be one of: stars, forks, updated")
    private String sort = "stars";

    public GitHubSearchRequest(String query, String language, String sort) {
        this.query = query;
        this.language = language;
        this.sort = sort != null ? sort : "stars";
    }
} 