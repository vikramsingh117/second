package com.github.repositorysearcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.repositorysearcher.dto.GitHubSearchRequest;
import com.github.repositorysearcher.dto.RepositoryDto;
import com.github.repositorysearcher.service.RepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GitHubController.class)
class GitHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepositoryService repositoryService;

    @Test
    void searchRepositories_ShouldReturnRepositories_WhenValidRequest() throws Exception {
        // Arrange
        GitHubSearchRequest request = new GitHubSearchRequest("spring", "Java", "stars");
        RepositoryDto repositoryDto = new RepositoryDto(123456L, "spring-boot", "Spring Boot framework",
                "spring-projects", "Java", 1000, 500, LocalDateTime.now());
        
        when(repositoryService.searchAndSaveRepositories(any(GitHubSearchRequest.class)))
                .thenReturn(List.of(repositoryDto));

        // Act & Assert
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Repositories fetched and saved successfully"))
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories[0].name").value("spring-boot"))
                .andExpect(jsonPath("$.repositories[0].owner").value("spring-projects"))
                .andExpect(jsonPath("$.repositories[0].language").value("Java"))
                .andExpect(jsonPath("$.repositories[0].stars").value(1000));
    }

    @Test
    void searchRepositories_ShouldReturnNoRepositoriesMessage_WhenEmptyResult() throws Exception {
        // Arrange
        GitHubSearchRequest request = new GitHubSearchRequest("nonexistent", "Java", "stars");
        
        when(repositoryService.searchAndSaveRepositories(any(GitHubSearchRequest.class)))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No repositories found for the given criteria"))
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories").isEmpty());
    }

    @Test
    void searchRepositories_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Arrange
        GitHubSearchRequest request = new GitHubSearchRequest("", "Java", "invalid");

        // Act & Assert
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRepositories_ShouldReturnRepositories_WhenNoFilters() throws Exception {
        // Arrange
        RepositoryDto repositoryDto = new RepositoryDto(123456L, "spring-boot", "Spring Boot framework",
                "spring-projects", "Java", 1000, 500, LocalDateTime.now());
        
        when(repositoryService.getRepositories(null, null, "stars"))
                .thenReturn(List.of(repositoryDto));

        // Act & Assert
        mockMvc.perform(get("/api/github/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories[0].name").value("spring-boot"));
    }

    @Test
    void getRepositories_ShouldReturnFilteredRepositories_WhenFiltersProvided() throws Exception {
        // Arrange
        RepositoryDto repositoryDto = new RepositoryDto(123456L, "spring-boot", "Spring Boot framework",
                "spring-projects", "Java", 1000, 500, LocalDateTime.now());
        
        when(repositoryService.getRepositories("Java", 100, "stars"))
                .thenReturn(List.of(repositoryDto));

        // Act & Assert
        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100")
                        .param("sort", "stars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories[0].language").value("Java"))
                .andExpect(jsonPath("$.repositories[0].stars").value(1000));
    }

    @Test
    void getRepositories_ShouldReturnEmptyArray_WhenNoRepositoriesFound() throws Exception {
        // Arrange
        when(repositoryService.getRepositories("NonExistentLanguage", 10000, "stars"))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "NonExistentLanguage")
                        .param("minStars", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories").isEmpty());
    }
} 