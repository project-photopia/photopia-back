package com.photopia.photopia_back.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {

    private int status;
    private String message;
    private String code;
}
