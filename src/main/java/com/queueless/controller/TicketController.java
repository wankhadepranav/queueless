package com.queueless.controller;
import com.queueless.dto.QueueStatusResponse;
import com.queueless.entity.Queue;
import com.queueless.entity.Ticket;
import com.queueless.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketService.saveTicket(ticket);
    }

 @PostMapping("/join")
public ResponseEntity<Ticket> joinQueue(
        @RequestParam Long userId,
        @RequestBody Queue queue) {

    Ticket ticket = ticketService.joinQueue(queue, userId);
    return ticket == null
            ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            : ResponseEntity.ok(ticket);
}
    @PostMapping("/queue/{queueId}/call-next")
    public Ticket callNext(@PathVariable Long queueId) {

        Queue queue = new Queue();
        queue.setId(queueId);

        return ticketService.callNext(queue);
    }

    @PostMapping("/{ticketId}/complete")
    public Ticket completeTicket(@PathVariable Long ticketId) {
        return ticketService.completeTicket(ticketId);
    }

    @PostMapping("/{ticketId}/cancel")
    public Ticket cancelTicket(@PathVariable Long ticketId) {
        return ticketService.cancelTicket(ticketId);
    }
    @GetMapping("/{ticketId}/position")
public int getQueuePosition(@PathVariable Long ticketId) {
    return ticketService.getQueuePosition(ticketId);
}
@GetMapping("/{ticketId}/estimated-wait")
public int getEstimatedWaitingTime(@PathVariable Long ticketId) {
    return ticketService.getEstimatedWaitingTime(ticketId);
}
@GetMapping("/{ticketId}/status")
public QueueStatusResponse getQueueStatus(@PathVariable Long ticketId) {
    return ticketService.getQueueStatus(ticketId);
}
}