    package com.enterprise.etl.service;

import com.enterprise.etl.model.StatusResponse;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EtlService {

    // In-memory storage for job statuses
    private ConcurrentHashMap<String, StatusResponse> jobStatus = new ConcurrentHashMap<>();

    public String startEtlJob() {
        String jobId = UUID.randomUUID().toString();
        // Mark job as started
        jobStatus.put(jobId, new StatusResponse(jobId, "Started", System.currentTimeMillis()));
        
        // Emulate a long-running ETL process in a separate thread
        new Thread(() -> {
            try {
                Thread.sleep(10000); // Emulate processing delay
                jobStatus.put(jobId, new StatusResponse(jobId, "Completed", System.currentTimeMillis()));
            } catch (InterruptedException e) {
                jobStatus.put(jobId, new StatusResponse(jobId, "Error", System.currentTimeMillis()));
            }
        }).start();
        
        return jobId;
    }

    public StatusResponse getJobStatus(String jobId) {
        return jobStatus.getOrDefault(jobId, new StatusResponse(jobId, "Not found", System.currentTimeMillis()));
    }
}
