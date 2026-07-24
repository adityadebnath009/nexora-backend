package com.aditya.nexora.userService.service;


import com.aditya.nexora.userService.dto.UsernameCheckResponseDTO;
import com.aditya.nexora.userService.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UsernameGenerator {

    private final UserRepository userRepository;


    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public String generateBaseUsername(String name) {
        if (name == null || name.isBlank()) {
            return "user";
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    List<String> generateUsernameSuggestions(String baseUsername) {
        List<String> suggestions = new ArrayList<>();
        while (suggestions.size() < 3) {
            int suffix = ThreadLocalRandom.current().nextInt(100, 10000);
            String candidate = baseUsername + "-" + suffix;
            if (!userRepository.existsByUsername(candidate) && !suggestions.contains(candidate)) {
                suggestions.add(candidate);
            }
        }
        return suggestions;
    }

    public UsernameCheckResponseDTO checkUsernameAvailability(String username) {
        if (username == null || username.isBlank()) {
            return new UsernameCheckResponseDTO(false, List.of());
        }

        String cleanUsername = username.toLowerCase().trim();
        boolean exists = userRepository.existsByUsername(cleanUsername);
        if (!exists) {
            return new UsernameCheckResponseDTO(true, List.of());
        } else {
            List<String> suggestions = generateUsernameSuggestions(cleanUsername);
            return new UsernameCheckResponseDTO(false, suggestions);
        }
    }


}
