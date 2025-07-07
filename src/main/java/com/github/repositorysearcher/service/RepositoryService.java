package com.github.repositorysearcher.service;

import com.github.repositorysearcher.dto.GitHubApiResponse;
import com.github.repositorysearcher.dto.GitHubSearchRequest;
import com.github.repositorysearcher.dto.RepositoryDto;
import com.github.repositorysearcher.entity.Repository;
import com.github.repositorysearcher.repository.RepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryService.class);

    private final GitHubApiService gitHubApiService;
    private final RepositoryRepository repositoryRepository;

    @Autowired
    public RepositoryService(GitHubApiService gitHubApiService, RepositoryRepository repositoryRepository) {
        this.gitHubApiService = gitHubApiService;
        this.repositoryRepository = repositoryRepository;
    }

    public List<RepositoryDto> searchAndSaveRepositories(GitHubSearchRequest request) {
        logger.info("Starting repository search and save operation for request: {}", request);

        // Fetch from GitHub API
        GitHubApiResponse apiResponse = gitHubApiService.searchRepositories(request);
        
        if (apiResponse.getItems() == null || apiResponse.getItems().isEmpty()) {
            logger.info("No repositories found for search query");
            return List.of();
        }

        // Convert and save repositories
        List<Repository> repositories = apiResponse.getItems().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        List<Repository> savedRepositories = saveOrUpdateRepositories(repositories);
        
        logger.info("Successfully processed {} repositories", savedRepositories.size());
        
        return savedRepositories.stream()
                .map(RepositoryDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RepositoryDto> getRepositories(String language, Integer minStars, String sort) {
        logger.info("Retrieving repositories with filters - language: {}, minStars: {}, sort: {}", 
                   language, minStars, sort);

        // Validate sort parameter
        String validSort = validateSortParameter(sort);

        List<Repository> repositories = repositoryRepository.findRepositoriesWithFilters(
                language, minStars, validSort);

        logger.info("Found {} repositories matching the criteria", repositories.size());
        
        return repositories.stream()
                .map(RepositoryDto::new)
                .collect(Collectors.toList());
    }

    private List<Repository> saveOrUpdateRepositories(List<Repository> repositories) {
        return repositories.stream()
                .map(this::saveOrUpdateRepository)
                .collect(Collectors.toList());
    }

    private Repository saveOrUpdateRepository(Repository repository) {
        Optional<Repository> existingRepo = repositoryRepository.findByRepositoryId(repository.getRepositoryId());
        
        if (existingRepo.isPresent()) {
            // Update existing repository
            Repository existing = existingRepo.get();
            updateRepositoryFields(existing, repository);
            Repository saved = repositoryRepository.save(existing);
            logger.debug("Updated existing repository: {}", saved.getName());
            return saved;
        } else {
            // Save new repository
            Repository saved = repositoryRepository.save(repository);
            logger.debug("Saved new repository: {}", saved.getName());
            return saved;
        }
    }

    private void updateRepositoryFields(Repository existing, Repository newData) {
        existing.setName(newData.getName());
        existing.setDescription(newData.getDescription());
        existing.setOwnerName(newData.getOwnerName());
        existing.setProgrammingLanguage(newData.getProgrammingLanguage());
        existing.setStarsCount(newData.getStarsCount());
        existing.setForksCount(newData.getForksCount());
        existing.setLastUpdatedDate(newData.getLastUpdatedDate());
        // Note: createdAt and updatedAt are handled by JPA annotations
    }

    private Repository convertToEntity(GitHubApiResponse.GitHubRepository githubRepo) {
        Repository repository = new Repository();
        repository.setRepositoryId(githubRepo.getId());
        repository.setName(githubRepo.getName());
        repository.setDescription(githubRepo.getDescription());
        repository.setOwnerName(githubRepo.getOwner() != null ? githubRepo.getOwner().getLogin() : "Unknown");
        repository.setProgrammingLanguage(githubRepo.getLanguage());
        repository.setStarsCount(githubRepo.getStargazersCount() != null ? githubRepo.getStargazersCount() : 0);
        repository.setForksCount(githubRepo.getForksCount() != null ? githubRepo.getForksCount() : 0);
        repository.setLastUpdatedDate(githubRepo.getUpdatedAt());
        return repository;
    }

    private String validateSortParameter(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return "stars";
        }
        
        String lowerSort = sort.toLowerCase().trim();
        return switch (lowerSort) {
            case "stars", "forks", "updated" -> lowerSort;
            default -> "stars";
        };
    }
} 