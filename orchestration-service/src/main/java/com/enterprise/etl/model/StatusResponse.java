package com.enterprise.etl.model;

public class StatusResponse {
    private String jobId;
    private String status;
    private long timestamp;

    public StatusResponse(String jobId, String status, long timestamp) {
        this.jobId = jobId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getJobId() {
        return jobId;
    }
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
