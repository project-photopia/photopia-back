package com.photopia.photopia_back.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.photopia.photopia_back.model.Album;
import com.photopia.photopia_back.model.User;


public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByUser(User user, Pageable pageable);

}
