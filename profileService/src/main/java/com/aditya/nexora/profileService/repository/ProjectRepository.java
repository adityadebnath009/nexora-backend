package com.aditya.nexora.profileService.repository;

import com.aditya.nexora.profileService.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserId(Long userId);

    List<Project> findByUserIdAndIsVisibleTrue(Long userId);

}
