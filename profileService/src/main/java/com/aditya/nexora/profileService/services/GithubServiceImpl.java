package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.dtos.*;
import com.aditya.nexora.profileService.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class GithubServiceImpl implements GithubService{


    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public GithubServiceImpl(@Value("${github.client-id}") String clientId, @Value("${github.client-secret}") String clientSecret, RestClient restClient) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = restClient;
    }

    @Override
    public String exchangeCodeForAccessToken(String code) {

        String url = "https://github.com/login/oauth/access_token";

        Map<String, String> params = Map.of("client_id", clientId, "client_secret", clientSecret, "code", code);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params,headers);

        try{
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(params)
                    .retrieve()
                    .body(Map.class);

            if(response!=null && response.containsKey("access_token")){
                return (String) response.get("access_token");
            }
            else {
                throw new BadRequestException("Github Access Token not found in response" + response.toString());
            }

        }
        catch (HttpClientErrorException e){
            log.error("Error while exchanging code for access token", e);
            throw new BadRequestException("GitHub OAuth code exchange failed: "+ e.getResponseBodyAsString());
        }
    }

    @Override
    public GitHubProfileDTO fetchProfile(String accessToken) {

        String url = "https://api.github.com/user";

        try {

            Map<String, Object> body = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.USER_AGENT, "Nexora")
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                throw new BadRequestException("Github Profile not found");
            }

            log.info("Github Profile: {}", body);
            return new GitHubProfileDTO(
                    getAsLong(body, "id"),
                    (String) body.get("login"),
                    (String) body.get("avatar_url"),
                    (String) body.get("bio"),
                    getAsInteger(body, "followers"),
                    getAsInteger(body, "following"),
                    getAsInteger(body, "public_repos"),
                    (String) body.get("location"),
                    (String) body.get("company"),
                    (String) body.get("blog")
            );


        }
        catch (HttpClientErrorException e){
            log.error("Error while fetching profile", e);
            throw new BadRequestException("GitHub Profile fetch failed: "+ e.getResponseBodyAsString());
        }

    }

    @Override
    public List<RepositoryDTO> fetchRepositories(String accessToken) {


        try {


            List<RepositoryDTO> dtos = new ArrayList<>();
            int page = 1;
            boolean hasMore = true;
            while (hasMore) {
                String paginatedUrl = "https://api.github.com/user/repos?per_page=100&type=owner&page=" +page;
                List<Map<String, Object>> reposList = restClient.get()
                        .uri(paginatedUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .header(HttpHeaders.USER_AGENT, "Nexora")
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

                if (reposList == null) {
                    return List.of();
                }
                for (Map<String, Object> repo : reposList) {
                    LocalDateTime createdAt = parseIsoDateTime((String) repo.get("created_at"));
                    LocalDateTime updatedAt = parseIsoDateTime((String) repo.get("updated_at"));
                    LocalDateTime pushedAt = parseIsoDateTime((String) repo.get("pushed_at"));

                    String visibility = Boolean.TRUE.equals(repo.get("private")) ? "PRIVATE" : "PUBLIC";

                    List<String> topics = (List<String>) repo.get("topics");
                    if (topics == null) {
                        topics = List.of();
                    }

                    Boolean isFork = (Boolean) repo.get("fork");
                    Boolean isArchived = (Boolean) repo.get("archived");
                    String defaultBranch = (String) repo.get("default_branch");
                    Map<String, Object>  owner = (Map<String, Object>) repo.get("owner");
                    String ownerLogin = owner!=null?(String) owner.get("login"):null;
                    String fullName = (String) repo.get("full_name");



                    dtos.add(new RepositoryDTO(
                            getAsLong(repo, "id"),
                            (String) repo.get("name"),
                            (String) repo.get("description"),
                            (String) repo.get("html_url"),
                            getAsInteger(repo, "stargazers_count"),
                            getAsInteger(repo, "forks_count"),
                            (String) repo.get("language"),
                            createdAt,
                            updatedAt,
                            visibility,
                            topics,
                            isFork != null ? isFork : false,
                            isArchived != null ? isArchived : false,
                            defaultBranch != null ? defaultBranch : "main",
                            pushedAt,
                            ownerLogin,
                            fullName
                    ));
                }

                if(reposList.size() < 100)
                {
                    hasMore = false;
                }
                page++;

            }


            return dtos;
        }
        catch (HttpClientErrorException e) {
            log.error("Error while fetching repositories", e);
            throw new BadRequestException("GitHub Repositories fetch failed: " + e.getResponseBodyAsString());
        }
    }

    @Override
    public String fetchReadme(String owner, String repo, String accessToken) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/readme";
        try{
            String respone = restClient.get().
                    uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.USER_AGENT, "Nexora")
                    .accept(MediaType.valueOf("application/vnd.github.v3.raw"))
                    .retrieve()
                    .body(String.class);

            return respone;


        }
        catch (HttpClientErrorException.NotFound e) {
            log.warn("README not found for repo: {}/{}", owner, repo);
            return null;
        }
        catch (HttpClientErrorException e){
            log.error("Error while fetching readme", e);
            throw new BadRequestException("No README found for this repository" + e.getMessage());
        }

    }

    @Override
    public Map<String, Long> fetchLanguages(String owner, String repo, String accessToken) {

        String url = "https://api.github.com/repos/{owner}/{repo}/languages";
        try{
            Map<String, Long> response = restClient.get()
                    .uri(url, owner, repo)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.USER_AGENT, "Nexora")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Long>>() {});
            return response;
        }
        catch (HttpClientErrorException.NotFound e) {
            log.warn("Languages not found for repo: {}/{}", owner, repo);
            return Map.of();
        }
        catch (HttpClientErrorException e){
            log.error("Error while fetching languages", e);
            throw new BadRequestException("No languages found for this repository: "+ e.getMessage());


        }

    }

    @Override
    public List<GitHubTreeItemDTO> fetchRepositoryTree(String owner, String repo, String branch, String accessToken) {
        String url = "https://api.github.com/repos/"+owner+"/"+repo+"/git/trees/"+branch+"?recursive=1";
        try{
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.USER_AGENT, "Nexora")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> tree = (List<Map<String, Object>>) response.get("tree");

            if(tree==null)
            {
                log.warn("Repository tree not found for repo: {}/{}", owner, repo);
                return List.of();
            }
            List<GitHubTreeItemDTO> gitHubTreeItemDTOS = new ArrayList<>();

            for(Map<String, Object> item:tree)
            {
                gitHubTreeItemDTOS.add(new GitHubTreeItemDTO(
                        (String) item.get("path"),
                        (String)item.get("type"),
                        item.get("size")!=null?getAsLong(item,"size"):0L,
                        (String)item.get("sha"),
                        (String)item.get("url")
                ));

            }

            log.info("Repository tree has been fetched successfully for repo: {}/{}", owner, repo);
            return gitHubTreeItemDTOS;
        }
        catch (HttpClientErrorException e){
            log.error("Error while fetching repository tree", e);
            throw new BadRequestException("No repository tree found for this repository: "+ e.getMessage());
        }

    }

    @Override
    public String fetchFileContent(String owner, String repo, String sha, String accessToken) {

        String url = "https://api.github.com/repos/"+owner+"/"+repo+"/git/blobs/"+sha;
        try
        {
            String response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.USER_AGENT, "Nexora")
                    .accept(MediaType.valueOf("application/vnd.github.v3.raw"))
                    .retrieve()
                    .body(String.class);

            return response;

        }
        catch (HttpClientErrorException.NotFound e) {
            log.warn("File content not found for repo: {}/{}", owner, repo);
            return null;
        }
        catch (HttpClientErrorException e){
            log.error("Error while fetching file content", e);
            throw new BadRequestException("No file content found for this repository: "+ e.getMessage());
        }

    }
    @Override
    public List<GitHubCommitDTO> fetchCommits(String owner, String repo, String accessToken) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/commits?per_page=30";
        try {
            List<Map<String, Object>> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.USER_AGENT, "Nexora")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response == null) {
                return List.of();
            }

            List<GitHubCommitDTO> commits = new ArrayList<>();
            for (Map<String, Object> item : response) {
                String sha = (String) item.get("sha");
                Map<String, Object> commitMap = (Map<String, Object>) item.get("commit");

                String message = "";
                String authorName = "";
                LocalDateTime date = null;

                if (commitMap != null) {
                    message = (String) commitMap.get("message");
                    Map<String, Object> authorMap = (Map<String, Object>) commitMap.get("author");
                    if (authorMap != null) {
                        authorName = (String) authorMap.get("name");
                        date = parseIsoDateTime((String) authorMap.get("date"));
                    }
                }

                Map<String, Object> githubAuthorMap = (Map<String, Object>) item.get("author");
                String authorLogin = null;
                if (githubAuthorMap != null) {
                    authorLogin = (String) githubAuthorMap.get("login");
                }

                commits.add(new GitHubCommitDTO(sha, message, authorName, authorLogin, date));
            }

            log.info("Successfully fetched {} commits for repository {}/{}", commits.size(), owner, repo);
            return commits;

        } catch (HttpClientErrorException e) {
            log.error("Error while fetching commits for repo {}/{}", owner, repo, e);
            throw new BadRequestException("GitHub Commits fetch failed: " + e.getMessage());
        }
    }

    @Override
    public List<GitHubPullRequestDTO> fetchPullRequests(String owner, String repo, String accessToken) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls?per_page=100";

        try
        {
            List<Map<String, Object>> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer "+accessToken)
                    .header(HttpHeaders.USER_AGENT,"Nexora")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});


            if(response==null)
            {
                return List.of();
            }
            List<GitHubPullRequestDTO> pulls = new ArrayList<>();

            for(Map<String, Object> item: response)
            {
                Long id = getAsLong(item,"id");
                String title = (String) item.get("title");
                String body = (String) item.get("body");
                String state = (String) item.get("state");

                Map<String, Object> user = (Map<String, Object>) item.get("user");
                String authorLogin = user!=null?(String) user.get("login"):null;

                LocalDateTime createdAt = parseIsoDateTime((String) item.get("created_at"));
                LocalDateTime mergedAt = parseIsoDateTime((String) item.get("merged_at"));

                pulls.add(new GitHubPullRequestDTO(id,title,body,state,authorLogin,createdAt,mergedAt));


            }
            log.info("Successfully fetched pull request {} for repository {}/{}", pulls.size(), owner, repo);
            return pulls;
        }
        catch (HttpClientErrorException e) {
            log.error("Error while fetching pull requests for repo {}/{}", owner, repo, e);
            throw new BadRequestException("GitHub Pull Requests fetch failed: " + e.getMessage());
        }

    }


    private Integer getAsInteger(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number num) {
            return num.intValue();
        }
        return 0;
    }
    private Long getAsLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number num) {
            return num.longValue();
        }
        return 0L;
    }

    private LocalDateTime parseIsoDateTime(String isoString) {
        if (isoString == null) return null;
        return LocalDateTime.ofInstant(java.time.Instant.parse(isoString), java.time.ZoneId.systemDefault());
    }
}
