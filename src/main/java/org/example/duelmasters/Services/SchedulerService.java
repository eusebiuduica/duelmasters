package org.example.duelmasters.Services;

import org.example.duelmasters.DTOs.Booster.BoosterResponse;
import org.example.duelmasters.Infrastructure.AllSseManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulerService {

    private final AllSseManager sseService;
    private final BoosterService boosterService;
    public SchedulerService(AllSseManager sseService, BoosterService boosterService) {
        this.sseService = sseService;
        this.boosterService = boosterService;
    }

    // runs every day at 12:00
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void sendDailyUpdate() {
        boosterService.resetAllQuantities();
        List<BoosterResponse> responses = boosterService.getAllBoosters();
        sseService.broadcast(responses, "DAILY_BOOSTERS_RESET");
    }
}
