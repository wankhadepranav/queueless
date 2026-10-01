package com.queueless.dto;

import com.queueless.entity.Queue;

public class QueueOptionResponse {

    private final Long id;
    private final String queueDate;
    private final String status;
    private final Long serviceId;
    private final String serviceName;
    private final Integer averageServiceTime;

    public QueueOptionResponse(Queue queue) {
        this.id = queue.getId();
        this.queueDate = queue.getQueueDate();
        this.status = queue.getStatus();
        this.serviceId = queue.getService().getId();
        this.serviceName = queue.getService().getName();
        this.averageServiceTime = queue.getService().getAverageServiceTime();
    }

    public Long getId() {
        return id;
    }

    public String getQueueDate() {
        return queueDate;
    }

    public String getStatus() {
        return status;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getAverageServiceTime() {
        return averageServiceTime;
    }
}