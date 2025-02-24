package com.enterprise.etl.controller;

import com.enterprise.etl.model.StatusResponse;
import com.enterprise.etl.service.EtlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etl")
public class EtlController {

    @Autowired
    private EtlService etlService;

    @PostMapping("/run")
    public StatusResponse runEtl() {
        String jobId = etlService.startEtlJob();
        return new StatusResponse(jobId, "Started", System.currentTimeMillis());
    }

    @GetMapping("/status/{jobId}")
    public StatusResponse getStatus(@PathVariable String jobId) {
        return etlService.getJobStatus(jobId);
    }
}
