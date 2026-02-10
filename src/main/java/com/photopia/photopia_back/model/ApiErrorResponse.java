package com.photopia.photopia_back.model;


public record ApiErrorResponse(boolean success, ApiError error) implements ApiResponse {

    public ApiErrorResponse(ApiError error) {
        this(false, error);
    }

    public static ApiErrorResponse of(ApiError error) {
        return new ApiErrorResponse(false, error);
    }
}
