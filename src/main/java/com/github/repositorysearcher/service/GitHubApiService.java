package com.github.repositorysearcher.service;

import com.github.repositorysearcher.dto.GitHubApiResponse;
import com.github.repositorysearcher.dto.GitHubSearchRequest;
import com.github.repositorysearcher.exception.GitHubApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class GitHubApiService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubApiService.class);

    private final WebClient webClient;
    private final String baseUrl;
    private final Duration timeout;

    public GitHubApiService(@Value("${github.api.base-url}") String baseUrl,
                           @Value("${github.api.timeout:30000}") long timeoutMs) {
        this.baseUrl = baseUrl;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public GitHubApiResponse searchRepositories(GitHubSearchRequest request) {
        logger.info("Searching GitHub repositories with request: {}", request);

        try {
            String queryString = buildQueryString(request);

            GitHubApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", queryString)
                            .queryParam("sort", request.getSort())
                            .queryParam("order", "desc")
                            .queryParam("per_page", 30)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatus.FORBIDDEN::equals, clientResponse -> {
                        logger.warn("GitHub API rate limit exceeded");
                        return Mono.error(new GitHubApiException("GitHub API rate limit exceeded. Please try again later."));
                    })
                    .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, clientResponse -> {
                        logger.warn("Invalid GitHub API request");
                        return Mono.error(new GitHubApiException("Invalid search query. Please check your request parameters."));
                    })
                    .onStatus(status -> status.is5xxServerError(), clientResponse -> {
                        logger.error("GitHub API server error");
                        return Mono.error(new GitHubApiException("GitHub API is currently unavailable. Please try again later."));
                    })
                    .bodyToMono(GitHubApiResponse.class)
                    .timeout(timeout)
                    .block();

            if (response == null) {
                throw new GitHubApiException("No response received from GitHub API");
            }

            logger.info("Successfully fetched {} repositories from GitHub API", 
                       response.getItems() != null ? response.getItems().size() : 0);
            return response;

        } catch (WebClientResponseException e) {
            logger.error("GitHub API request failed with status: {} and body: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new GitHubApiException("GitHub API request failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while calling GitHub API", e);
            throw new GitHubApiException("Failed to search repositories: " + e.getMessage());
        }
    }

    private String buildQueryString(GitHubSearchRequest request) {
        StringBuilder queryBuilder = new StringBuilder(request.getQuery());
        
        if (request.getLanguage() != null && !request.getLanguage().trim().isEmpty()) {
            queryBuilder.append(" language:").append(request.getLanguage().trim());
        }
        
        return queryBuilder.toString();
    }

    private Map<String, Object> buildQueryParams(GitHubSearchRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("sort", request.getSort());
        params.put("order", "desc");
        params.put("per_page", 30);
        return params;
    }
} 