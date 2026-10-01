package com.queueless.repository;

import com.queueless.entity.Queue;
import com.queueless.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByQueue(Queue queue);

    Ticket findFirstByQueueAndStatusOrderByIdAsc(Queue queue, String status);

    List<Ticket> findByQueueAndStatusOrderByIdAsc(Queue queue, String status);
}