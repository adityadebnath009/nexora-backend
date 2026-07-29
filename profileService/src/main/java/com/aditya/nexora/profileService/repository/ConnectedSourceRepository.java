package com.aditya.nexora.profileService.repository;

import com.aditya.nexora.profileService.entity.ConnectedSource;
import com.aditya.nexora.profileService.enums.SourceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectedSourceRepository extends JpaRepository<ConnectedSource, Long> {
    Optional<ConnectedSource> findByUserIdAndProvider(Long userId, SourceProvider provider);
    List<ConnectedSource> findByUserId(Long userId);
}
