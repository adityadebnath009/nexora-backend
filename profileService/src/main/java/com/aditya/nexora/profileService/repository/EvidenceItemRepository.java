package com.aditya.nexora.profileService.repository;

import com.aditya.nexora.profileService.entity.EvidenceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenceItemRepository extends JpaRepository<EvidenceItem, Long> {
    List<EvidenceItem> findByProjectUserId(Long userId);
    List<EvidenceItem> findByProjectId(Long projectId);
}
