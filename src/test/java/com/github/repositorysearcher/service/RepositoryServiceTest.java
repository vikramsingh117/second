package com.github.repositorysearcher.service;

import com.github.repositorysearcher.dto.GitHubApiResponse;
import com.github.repositorysearcher.dto.GitHubSearchRequest;
import com.github.repositorysearcher.dto.RepositoryDto;
import com.github.repositorysearcher.entity.Repository;
import com.github.repositorysearcher.repository.RepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private GitHubApiService gitHubApiService;

    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private RepositoryService repositoryService;

    private GitHubSearchRequest searchRequest;
    private GitHubApiResponse apiResponse;
    private Repository repository;

    @BeforeEach
    void setUp() {
        searchRequest = new GitHubSearchRequest("spring", "Java", "stars");
        
        // Create mock API response
        apiResponse = new GitHubApiResponse();
        apiResponse.setTotalCount(1);
        
        GitHubApiResponse.GitHubRepository githubRepo = new GitHubApiResponse.GitHubRepository();
        githubRepo.setId(123456L);
        githubRepo.setName("spring-boot");
        githubRepo.setDescription("Spring Boot framework");
        githubRepo.setLanguage("Java");
        githubRepo.setStargazersCount(1000);
        githubRepo.setForksCount(500);
        githubRepo.setUpdatedAt(LocalDateTime.now());
        
        GitHubApiResponse.GitHubRepository.Owner owner = new GitHubApiResponse.GitHubRepository.Owner();
        owner.setLogin("spring-projects");
        githubRepo.setOwner(owner);
        
        apiResponse.setItems(List.of(githubRepo));
        
        // Create mock repository entity
        repository = new Repository(123456L, "spring-boot", "Spring Boot framework", 
                                  "spring-projects", "Java", 1000, 500, LocalDateTime.now());
    }

    @Test
    void searchAndSaveRepositories_ShouldReturnRepositories_WhenValidRequest() {
        // Arrange
        when(gitHubApiService.searchRepositories(searchRequest)).thenReturn(apiResponse);
        when(repositoryRepository.findByRepositoryId(123456L)).thenReturn(Optional.empty());
        when(repositoryRepository.save(any(Repository.class))).thenReturn(repository);

        // Act
        List<RepositoryDto> result = repositoryService.searchAndSaveRepositories(searchRequest);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("spring-boot");
        assertThat(result.get(0).getOwner()).isEqualTo("spring-projects");
        assertThat(result.get(0).getLanguage()).isEqualTo("Java");
        assertThat(result.get(0).getStars()).isEqualTo(1000);
        
        verify(gitHubApiService).searchRepositories(searchRequest);
        verify(repositoryRepository).save(any(Repository.class));
    }

    @Test
    void searchAndSaveRepositories_ShouldUpdateExistingRepository_WhenRepositoryExists() {
        // Arrange
        Repository existingRepo = new Repository(123456L, "old-name", "old-description", 
                                               "old-owner", "Java", 800, 400, LocalDateTime.now().minusDays(1));
        
        when(gitHubApiService.searchRepositories(searchRequest)).thenReturn(apiResponse);
        when(repositoryRepository.findByRepositoryId(123456L)).thenReturn(Optional.of(existingRepo));
        when(repositoryRepository.save(any(Repository.class))).thenReturn(repository);

        // Act
        List<RepositoryDto> result = repositoryService.searchAndSaveRepositories(searchRequest);

        // Assert
        assertThat(result).hasSize(1);
        verify(repositoryRepository).save(existingRepo);
        // Verify that existing repository was updated
        assertThat(existingRepo.getName()).isEqualTo("spring-boot");
        assertThat(existingRepo.getStarsCount()).isEqualTo(1000);
    }

    @Test
    void searchAndSaveRepositories_ShouldReturnEmptyList_WhenNoRepositoriesFound() {
        // Arrange
        GitHubApiResponse emptyResponse = new GitHubApiResponse();
        emptyResponse.setItems(List.of());
        
        when(gitHubApiService.searchRepositories(searchRequest)).thenReturn(emptyResponse);

        // Act
        List<RepositoryDto> result = repositoryService.searchAndSaveRepositories(searchRequest);

        // Assert
        assertThat(result).isEmpty();
        verify(repositoryRepository, never()).save(any());
    }

    @Test
    void getRepositories_ShouldReturnFilteredRepositories_WhenValidFilters() {
        // Arrange
        List<Repository> repositories = List.of(repository);
        when(repositoryRepository.findRepositoriesWithFilters("Java", 100, "stars"))
                .thenReturn(repositories);

        // Act
        List<RepositoryDto> result = repositoryService.getRepositories("Java", 100, "stars");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLanguage()).isEqualTo("Java");
        verify(repositoryRepository).findRepositoriesWithFilters("Java", 100, "stars");
    }

    @Test
    void getRepositories_ShouldUseDefaultSort_WhenInvalidSortProvided() {
        // Arrange
        List<Repository> repositories = List.of(repository);
        when(repositoryRepository.findRepositoriesWithFilters(null, null, "stars"))
                .thenReturn(repositories);

        // Act
        List<RepositoryDto> result = repositoryService.getRepositories(null, null, "invalid");

        // Assert
        assertThat(result).hasSize(1);
        verify(repositoryRepository).findRepositoriesWithFilters(null, null, "stars");
    }

    @Test
    void validateSortParameter_ShouldReturnStars_WhenNullOrEmptySort() {
        // This tests the private method indirectly through getRepositories
        List<Repository> repositories = List.of(repository);
        when(repositoryRepository.findRepositoriesWithFilters(null, null, "stars"))
                .thenReturn(repositories);

        repositoryService.getRepositories(null, null, null);
        repositoryService.getRepositories(null, null, "");
        repositoryService.getRepositories(null, null, "  ");

        verify(repositoryRepository, times(3)).findRepositoriesWithFilters(null, null, "stars");
    }
} 