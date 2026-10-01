package com.queueless.service;

import com.queueless.dto.QueueStatusResponse;
import com.queueless.entity.Queue;
import com.queueless.entity.Ticket;
import com.queueless.entity.User;
import com.queueless.repository.QueueRepository;
import com.queueless.repository.TicketRepository;
import com.queueless.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final QueueRepository queueRepository;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            QueueRepository queueRepository) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Ticket joinQueue(Queue queue, Long userId) {

        if (queue == null || queue.getId() == null) {
            return null;
        }

        User user = userRepository.findById(userId).orElse(null);
        Queue persistedQueue = queueRepository.findById(queue.getId()).orElse(null);

        if (user == null || persistedQueue == null) {
            return null;
        }

        List<Ticket> tickets = ticketRepository.findByQueue(persistedQueue);

        int nextNumber = tickets.size() + 1;

        String tokenNumber = String.format("A%03d", nextNumber);

        Ticket ticket = new Ticket();

        ticket.setTokenNumber(tokenNumber);
        ticket.setStatus("WAITING");
        ticket.setQueue(persistedQueue);
        ticket.setUser(user);
        ticket.setCreatedAt(java.time.LocalDateTime.now().toString());

        return ticketRepository.save(ticket);
    }

    public Ticket callNext(Queue queue) {

        Ticket ticket = ticketRepository
                .findFirstByQueueAndStatusOrderByIdAsc(queue, "WAITING");

        if (ticket == null) {
            return null;
        }

        ticket.setStatus("CALLED");
        ticket.setCalledAt(java.time.LocalDateTime.now().toString());

        return ticketRepository.save(ticket);
    }

    public Ticket completeTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

        if (ticket == null) {
            return null;
        }

        ticket.setStatus("COMPLETED");
        ticket.setCompletedAt(java.time.LocalDateTime.now().toString());

        return ticketRepository.save(ticket);
    }

    public Ticket cancelTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

        if (ticket == null) {
            return null;
        }

        if (!ticket.getStatus().equals("WAITING")) {
            return null;
        }

        ticket.setStatus("CANCELLED");

        return ticketRepository.save(ticket);
    }

    public int getQueuePosition(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

        if (ticket == null) {
            return -1;
        }

        List<Ticket> waitingTickets =
                ticketRepository.findByQueueAndStatusOrderByIdAsc(
                        ticket.getQueue(),
                        "WAITING"
                );

        for (int i = 0; i < waitingTickets.size(); i++) {

            if (waitingTickets.get(i).getId().equals(ticketId)) {
                return i + 1;
            }
        }

        return -1;
    }

    public int getEstimatedWaitingTime(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

        if (ticket == null) {
            return -1;
        }

        int position = getQueuePosition(ticketId);

        if (position == -1) {
            return -1;
        }

        Integer averageServiceTime =
                ticket.getQueue().getService().getAverageServiceTime();

        if (averageServiceTime == null) {
            return -1;
        }

        return (position - 1) * averageServiceTime;
    }

    public QueueStatusResponse getQueueStatus(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

        if (ticket == null) {
            return null;
        }

        int position = getQueuePosition(ticketId);

        int peopleAhead = position > 0 ? position - 1 : 0;

        int estimatedWaitMinutes = getEstimatedWaitingTime(ticketId);

        return new QueueStatusResponse(
                ticket.getTokenNumber(),
                ticket.getStatus(),
                position,
                peopleAhead,
                estimatedWaitMinutes
        );
    }
}