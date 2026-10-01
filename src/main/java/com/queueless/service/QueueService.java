package com.queueless.service;

import com.queueless.entity.Queue;
import com.queueless.repository.QueueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {

    private final QueueRepository queueRepository;

    public QueueService(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    public List<Queue> getAllQueues() {
        return queueRepository.findAll();
    }

    public Queue saveQueue(Queue queue) {
        return queueRepository.save(queue);
    }
}