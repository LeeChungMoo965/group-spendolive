package com.example.spendolive.ott.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.spendolive.ott.service.OttService;

@Component
public class OttScheduleTask {

    private final OttService ottService;

    public OttScheduleTask(OttService ottService) {
        this.ottService = ottService;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void processOttPaymentAndCloseJobs() {
        ottService.processScheduledOttJobs();
    }
}
