package com.photopia.photopia_back.model;

public record ApiSuccessResponse(boolean success, Object data, String message) implements ApiResponse {

    public ApiSuccessResponse(Object data, String message) {
        this(true, data, message);
    }

    public static ApiSuccessResponse of(Object data, String message) {
        return new ApiSuccessResponse(true, data, message);
    }
}
