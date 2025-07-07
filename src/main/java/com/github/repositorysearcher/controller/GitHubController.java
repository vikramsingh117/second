package com.github.repositorysearcher.controller;

import com.github.repositorysearcher.dto.ApiResponse;
import com.github.repositorysearcher.dto.GitHubSearchRequest;
import com.github.repositorysearcher.dto.RepositoryDto;
import com.github.repositorysearcher.service.RepositoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private static final Logger logger = LoggerFactory.getLogger(GitHubController.class);

    private final RepositoryService repositoryService;

    @Autowired
    public GitHubController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse.SearchResponse> searchRepositories(
            @Valid @RequestBody GitHubSearchRequest request) {
        
        logger.info("Received search request: {}", request);
        
        List<RepositoryDto> repositories = repositoryService.searchAndSaveRepositories(request);
        
        String message = repositories.isEmpty() ? 
                "No repositories found for the given criteria" : 
                "Repositories fetched and saved successfully";
        
        ApiResponse.SearchResponse response = new ApiResponse.SearchResponse(message, repositories);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/repositories")
    public ResponseEntity<ApiResponse.RepositoriesResponse> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(defaultValue = "stars") String sort) {
        
        logger.info("Received get repositories request - language: {}, minStars: {}, sort: {}", 
                   language, minStars, sort);
        
        List<RepositoryDto> repositories = repositoryService.getRepositories(language, minStars, sort);
        
        ApiResponse.RepositoriesResponse response = new ApiResponse.RepositoriesResponse(repositories);
        
        return ResponseEntity.ok(response);
    }
} 