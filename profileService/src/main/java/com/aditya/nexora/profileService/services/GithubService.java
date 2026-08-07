package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.dtos.GitHubCommitDTO;
import com.aditya.nexora.profileService.dtos.GitHubProfileDTO;
import com.aditya.nexora.profileService.dtos.GitHubTreeItemDTO;
import com.aditya.nexora.profileService.dtos.RepositoryDTO;

import java.util.List;
import java.util.Map;

public interface GithubService {
    String exchangeCodeForAccessToken(String code);
    GitHubProfileDTO fetchProfile(String accessToken);
    List<RepositoryDTO> fetchRepositories(String accessToken);

    // Add to GithubService.java
    String fetchReadme(String owner, String repo, String accessToken);

    // Add to GithubService.java
    Map<String, Long> fetchLanguages(String owner, String repo, String accessToken);

    List<GitHubTreeItemDTO> fetchRepositoryTree(String owner, String repo, String branch, String accessToken);

    String fetchFileContent(String owner, String repo, String sha, String accessToken);

    List<GitHubCommitDTO> fetchCommits(String owner, String repo, String accessToken);

}
