\# Queueless — Queue Management System



Queueless is a Spring Boot-based queue management system that allows customers to join service queues and track their queue status while staff members manage tickets and administrators manage users, services, and queues.



\## Features



\- User registration and login

\- JWT-based authentication

\- BCrypt password hashing

\- Role-based authorization

\- Customer queue joining

\- Automatic ticket/token generation

\- Queue position tracking

\- Estimated waiting time

\- Queue status tracking

\- Staff call-next functionality

\- Ticket completion and cancellation

\- Queue management

\- Service management

\- User management



\## User Roles



\### CUSTOMER

\- Login

\- Join a queue

\- View ticket position

\- View estimated waiting time

\- View ticket status

\- Cancel a waiting ticket



\### STAFF

\- Login

\- Call the next waiting customer

\- Complete tickets

\- Perform queue operations



\### ADMIN

\- Manage users

\- Manage services

\- Manage queues

\- Perform staff-level operations



\## Tech Stack



\- Java 21

\- Spring Boot

\- Spring Web

\- Spring Data JPA

\- Hibernate

\- MySQL

\- Spring Security Crypto

\- BCrypt

\- JWT

\- Maven



\## Architecture



```text

Client

&#x20; |

&#x20; v

REST Controllers

&#x20; |

&#x20; v

Service Layer

&#x20; |

&#x20; v

JPA Repositories

&#x20; |

&#x20; v

MySQL Database

