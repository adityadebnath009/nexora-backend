package com.aditya.nexora.profileService.scheduler;

import com.aditya.nexora.profileService.entity.ConnectedSource;
import com.aditya.nexora.profileService.repository.ConnectedSourceRepository;
import com.aditya.nexora.profileService.services.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class GithubSyncScheduler {

    private final ConnectedSourceRepository connectedSourceRepository;
    private final ProfileService profileService;

    public GithubSyncScheduler(ConnectedSourceRepository connectedSourceRepository, ProfileService profileService) {
        this.connectedSourceRepository = connectedSourceRepository;
        this.profileService = profileService;
    }

    // Cron job to run every day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledGithubSync() {
        log.info("Starting scheduled GitHub metadata synchronization...");
        
        List<ConnectedSource> connectedSources = connectedSourceRepository.findAll();
        int successCount = 0;
        
        for (ConnectedSource source : connectedSources) {
            try {
                log.info("Scheduled sync triggered for user ID: {}", source.getUserId());
                profileService.syncGithubRepositories(source.getUserId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed scheduled GitHub sync for user ID: {}", source.getUserId(), e);
            }
        }
        
        log.info("Scheduled GitHub sync completed. Successfully synced {} out of {} connections.", 
                successCount, connectedSources.size());
    }
}