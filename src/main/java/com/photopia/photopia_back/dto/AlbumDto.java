package com.photopia.photopia_back.dto;

import com.photopia.photopia_back.model.Album;

public record AlbumDto(Long id, String name) {
    public static AlbumDto from(Album a) {
        return new AlbumDto(a.getId(), a.getName());
    }
}
