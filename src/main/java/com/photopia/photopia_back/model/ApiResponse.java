package com.photopia.photopia_back.model;

import lombok.Builder;
import lombok.Data;

@Data // @getters - @setters - toString() - equals() / hashCode() - constructor
@Builder
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;

}
