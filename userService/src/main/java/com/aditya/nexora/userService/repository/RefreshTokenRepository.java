package com.aditya.nexora.userService.repository;


import com.aditya.nexora.userService.entity.RefreshToken;
import com.aditya.nexora.userService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByToken(String token);
    void deleteByToken(String token);
    void deleteByUser(User user);
}
