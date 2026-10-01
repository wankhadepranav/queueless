package com.queueless.controller;

import com.queueless.entity.Queue;
import com.queueless.dto.QueueOptionResponse;
import com.queueless.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping
    public List<Queue> getAllQueues() {
        return queueService.getAllQueues();
    }

    @GetMapping("/options")
    public List<QueueOptionResponse> getQueueOptions() {
        return queueService.getAllQueues().stream()
                .filter(queue -> queue.getService() != null)
                .map(QueueOptionResponse::new)
                .toList();
    }

    @PostMapping
    public Queue createQueue(@RequestBody Queue queue) {
        return queueService.saveQueue(queue);
    }
}