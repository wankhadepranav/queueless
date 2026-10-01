# Queueless — Queue Management System

Queueless is a Spring Boot REST backend for digital queue management. Customers can register, join service queues, track their tickets, and cancel waiting tickets. Staff can call the next customer and complete or cancel tickets, while administrators can manage users, services, and queues.

The project uses JWT authentication, role-based authorization, JPA/Hibernate persistence, and MySQL for application data.

## Features

### Customer

- Register and log in
- Join an available service queue
- Receive an automatically generated ticket number
- View queue position and people ahead
- View estimated waiting time
- Track ticket status
- Cancel a waiting ticket

### Staff

- Log in with staff credentials
- View live queues
- See waiting customers and the next ticket
- Call the next waiting customer
- Complete served tickets
- Cancel tickets when required
- Review recent ticket activity

### Admin

- Manage users
- Manage services
- Manage queues
- Perform staff-level queue operations

## Queue Flow

```text
Customer
   |
   | Join service queue
   v
Ticket created
   |
   v
Waiting in queue
   |
   | Staff: Call next
   v
Currently called / Served
   |
   +----> Complete ----> COMPLETED
   |
   +----> Cancel ------> CANCELLED