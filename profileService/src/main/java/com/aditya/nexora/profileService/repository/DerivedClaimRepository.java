package com.aditya.nexora.profileService.repository;

import com.aditya.nexora.profileService.entity.DerivedClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DerivedClaimRepository extends JpaRepository<DerivedClaim, Long> {
    List<DerivedClaim> findByUserId(Long userId);
}