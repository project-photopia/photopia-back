package com.photopia.photopia_back.model;


public sealed interface ApiResponse permits ApiSuccessResponse, ApiErrorResponse {
}
