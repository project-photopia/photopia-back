package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.AlbumDto;
import com.photopia.photopia_back.repository.AlbumRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumRepository albumRepository;

    public AlbumController(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @GetMapping
    public List<AlbumDto> getAllAlbums() {
        return albumRepository.findAll()
                .stream()
                .map(AlbumDto::from)
                .toList();
    }
}
