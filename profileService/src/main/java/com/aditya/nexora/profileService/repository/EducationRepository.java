package com.aditya.nexora.profileService.repository;

import com.aditya.nexora.profileService.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByUserIdOrderByStartDateDesc(Long userId);
}