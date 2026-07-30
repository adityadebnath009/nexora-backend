package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.entity.ConnectedSource;

public interface ProfileService {
    ConnectedSource connectGithub(Long userId, String authorizationCode);
    void syncGithubRepositories(Long userId);


}
