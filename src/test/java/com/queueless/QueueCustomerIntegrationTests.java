package com.queueless;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.queueless.entity.User;
import com.queueless.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class QueueCustomerIntegrationTests {

        @Value("${local.server.port}")
        private int port;

        @Autowired
        private UserRepository userRepository;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final HttpClient httpClient = HttpClient.newHttpClient();
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void customerCanSelectQueueJoinRefreshAndCancelWithoutQueueManagementAccess() throws Exception {
        UserSession admin = registerAndLogin("ADMIN");
        UserSession customer = registerAndLogin("CUSTOMER");

        HttpResponse<String> adminPasswordReset = request(
                "PUT",
                "/api/users/" + admin.id() + "/password",
                admin.token(),
                Map.of("newPassword", "Admin123"));
        assertThat(adminPasswordReset.statusCode()).isEqualTo(204);

        User adminRecord = userRepository.findById(admin.id()).orElseThrow();
        assertThat(adminRecord.getRole()).isEqualTo("ADMIN");
        assertThat(adminRecord.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("Admin123", adminRecord.getPassword())).isTrue();

        HttpResponse<String> adminLogin = request(
                "POST",
                "/api/auth/login",
                null,
                Map.of("email", admin.email(), "password", "Admin123"));
        assertThat(adminLogin.statusCode()).isEqualTo(200);
        JsonNode adminLoginResponse = objectMapper.readTree(adminLogin.body());
        assertThat(adminLoginResponse.get("role").asText()).isEqualTo("ADMIN");
        assertThat(adminLoginResponse.get("token").asText()).isNotBlank();
        admin = new UserSession(
                admin.id(),
                admin.email(),
                adminLoginResponse.get("token").asText());

        HttpResponse<String> staffRegistration = request(
                "POST",
                "/api/auth/register",
                null,
                Map.of(
                        "name", "Staff User",
                        "email", "staff@test.com",
                        "password", "OldPassword123",
                        "role", "STAFF"));
        assertThat(staffRegistration.statusCode()).isEqualTo(201);
        long staffId = objectMapper.readTree(staffRegistration.body()).get("id").asLong();

        HttpResponse<String> customerResetAttempt = request(
                "PUT",
                "/api/users/" + staffId + "/password",
                customer.token(),
                Map.of("newPassword", "Staff123"));
        assertThat(customerResetAttempt.statusCode()).isEqualTo(403);

        HttpResponse<String> reset = request(
                "PUT",
                "/api/users/" + staffId + "/password",
                admin.token(),
                Map.of("newPassword", "Staff123"));
        assertThat(reset.statusCode()).isEqualTo(204);

        User staffRecord = userRepository.findById(staffId).orElseThrow();
        assertThat(staffRecord.getRole()).isEqualTo("STAFF");
        assertThat(staffRecord.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("Staff123", staffRecord.getPassword())).isTrue();

        HttpResponse<String> staffLogin = request(
                "POST",
                "/api/auth/login",
                null,
                Map.of("email", "staff@test.com", "password", "Staff123"));
        assertThat(staffLogin.statusCode()).isEqualTo(200);
        JsonNode staffResponse = objectMapper.readTree(staffLogin.body());
        assertThat(staffResponse.get("role").asText()).isEqualTo("STAFF");
        String staffToken = staffResponse.get("token").asText();
        assertThat(staffToken).isNotBlank();

        HttpResponse<String> customerLogin = request(
                "POST",
                "/api/auth/login",
                null,
                Map.of("email", customer.email(), "password", "test-password"));
        assertThat(customerLogin.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(customerLogin.body()).get("role").asText())
                .isEqualTo("CUSTOMER");

        HttpResponse<String> serviceResponse = request(
                "POST",
                "/api/services",
                admin.token(),
                Map.of("name", "Integration Service", "averageServiceTime", 12));
        assertThat(serviceResponse.statusCode()).isEqualTo(200);
        long serviceId = objectMapper.readTree(serviceResponse.body()).get("id").asLong();

        HttpResponse<String> queueResponse = request(
                "POST",
                "/api/queues",
                admin.token(),
                Map.of(
                        "queueDate", "2026-09-30",
                        "status", "OPEN",
                        "service", Map.of("id", serviceId)));
        assertThat(queueResponse.statusCode()).isEqualTo(200);
        long queueId = objectMapper.readTree(queueResponse.body()).get("id").asLong();

        HttpResponse<String> managementQueues = request(
                "GET", "/api/queues", customer.token(), null);
        assertThat(managementQueues.statusCode()).isEqualTo(403);

        HttpResponse<String> queueOptions = request(
                "GET", "/api/queues/options", customer.token(), null);
        assertThat(queueOptions.statusCode()).isEqualTo(200);
        JsonNode options = objectMapper.readTree(queueOptions.body());
        assertThat(options).hasSize(1);
        assertThat(options.get(0).get("id").asLong()).isEqualTo(queueId);
        assertThat(options.get(0).get("serviceId").asLong()).isEqualTo(serviceId);

        HttpResponse<String> joinResponse = request(
                "POST",
                "/api/tickets/join?userId=" + customer.id(),
                customer.token(),
                Map.of("id", queueId));
        assertThat(joinResponse.statusCode()).isEqualTo(200);
        JsonNode ticket = objectMapper.readTree(joinResponse.body());
        long ticketId = ticket.get("id").asLong();
        assertThat(ticket.get("queue").get("id").asLong()).isEqualTo(queueId);

        HttpResponse<String> status = request(
                "GET",
                "/api/tickets/" + ticketId + "/status",
                customer.token(),
                null);
        assertThat(status.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(status.body()).get("status").asText())
                .isEqualTo("WAITING");

        HttpResponse<String> position = request(
                "GET",
                "/api/tickets/" + ticketId + "/position",
                customer.token(),
                null);
        assertThat(position.body()).isEqualTo("1");

        HttpResponse<String> wait = request(
                "GET",
                "/api/tickets/" + ticketId + "/estimated-wait",
                customer.token(),
                null);
        assertThat(wait.body()).isEqualTo("0");

        HttpResponse<String> cancelled = request(
                "POST",
                "/api/tickets/" + ticketId + "/cancel",
                customer.token(),
                null);
        assertThat(cancelled.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(cancelled.body()).get("status").asText())
                .isEqualTo("CANCELLED");

        HttpResponse<String> invalidQueue = request(
                "POST",
                "/api/tickets/join?userId=" + customer.id(),
                customer.token(),
                Map.of("id", Long.MAX_VALUE));
        assertThat(invalidQueue.statusCode()).isEqualTo(404);

        HttpResponse<String> customerCallNext = request(
                "POST",
                "/api/tickets/queue/" + queueId + "/call-next",
                customer.token(),
                null);
        assertThat(customerCallNext.statusCode()).isEqualTo(403);

        HttpResponse<String> secondJoin = request(
                "POST",
                "/api/tickets/join?userId=" + customer.id(),
                customer.token(),
                Map.of("id", queueId));
        long secondTicketId = objectMapper.readTree(secondJoin.body()).get("id").asLong();

        HttpResponse<String> called = request(
                "POST",
                "/api/tickets/queue/" + queueId + "/call-next",
                staffToken,
                null);
        assertThat(called.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(called.body()).get("status").asText())
                .isEqualTo("CALLED");
        long calledTicketId = objectMapper.readTree(called.body()).get("id").asLong();
        assertThat(calledTicketId).isEqualTo(secondTicketId);

        HttpResponse<String> completed = request(
                "POST",
                "/api/tickets/" + calledTicketId + "/complete",
                staffToken,
                null);
        assertThat(completed.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(completed.body()).get("status").asText())
                .isEqualTo("COMPLETED");
    }

    private UserSession registerAndLogin(String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@example.test";
        HttpResponse<String> registration = request(
                "POST",
                "/api/auth/register",
                null,
                Map.of(
                        "name", "Integration " + role,
                        "email", email,
                        "password", "test-password",
                        "role", role));
        assertThat(registration.statusCode()).isEqualTo(201);

        HttpResponse<String> login = request(
                "POST",
                "/api/auth/login",
                null,
                Map.of("email", email, "password", "test-password"));
        assertThat(login.statusCode()).isEqualTo(200);
        JsonNode response = objectMapper.readTree(login.body());
        return new UserSession(
                response.get("id").asLong(),
                response.get("email").asText(),
                response.get("token").asText());
    }

    private HttpResponse<String> request(
            String method,
            String path,
            String token,
            Object body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        String requestBody = body == null ? "" : objectMapper.writeValueAsString(body);
        builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(requestBody));
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

        private record UserSession(long id, String email, String token) {}
}