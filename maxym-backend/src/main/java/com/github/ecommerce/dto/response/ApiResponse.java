package com.github.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Builder.Default
    private boolean success = true;
    
    private String message;
    
    private T data;
    
    private Object errors;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String path;
    
    private Integer status;
    
    // Static factory methods for common responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .status(200)
            .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .status(200)
            .build();
    }
    
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message("Resource created successfully")
            .status(201)
            .build();
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .status(201)
            .build();
    }
    
    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
            .success(true)
            .message("Operation completed successfully")
            .status(204)
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(400)
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(errors)
            .status(400)
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message, int status) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(status)
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message, Object errors, int status) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(errors)
            .status(status)
            .build();
    }
    
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(401)
            .build();
    }
    
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(403)
            .build();
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(404)
            .build();
    }
    
    public static <T> ApiResponse<T> conflict(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(409)
            .build();
    }
    
    public static <T> ApiResponse<T> internalError(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(500)
            .build();
    }
}