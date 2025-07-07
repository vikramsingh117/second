package com.github.repositorysearcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String message;
    private T data;
    private boolean success;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, true);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, false);
    }

    // Specific response types for our API
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResponse {
        private String message;
        private List<RepositoryDto> repositories;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepositoriesResponse {
        private List<RepositoryDto> repositories;
    }
} 