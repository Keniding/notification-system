package com.keniding.notificationSystem.controller;

import com.keniding.notificationSystem.consumer.AnalyticsConsumer;
import com.keniding.notificationSystem.dto.UserRegisteredEvent;
import com.keniding.notificationSystem.dto.UserRegistrationRequest;
import com.keniding.notificationSystem.producer.UserEventProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserEventProducer userEventProducer;
    private final AnalyticsConsumer analyticsConsumer;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            log.info("Received user registration request for: {}", request.getEmail());

            UserRegisteredEvent event = new UserRegisteredEvent(
                    UUID.randomUUID().toString(),
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getCountry() != null ? request.getCountry() : "DEFAULT",
                    LocalDateTime.now(),
                    "web"
            );

            userEventProducer.publishUserRegistered(event);
            log.info("User registration event published for: {}", request.getEmail());
            return ResponseEntity.ok("User registration successfully! Notifications will be sent shortly");
        } catch (Exception e) {
            log.error("Error occurred while processing user registration request for: {}", request.getEmail(), e);
            return ResponseEntity.internalServerError().body("Error registering user. Please try again later");
        }
    }

    @PostMapping("/register/sync")
    public ResponseEntity<String> registerUserSync(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            log.info("Received synchronous user registration request for: {}", request.getEmail());

            UserRegisteredEvent event = new UserRegisteredEvent(
                    UUID.randomUUID().toString(),
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getCountry() != null ? request.getCountry() : "DEFAULT",
                    LocalDateTime.now(),
                    "web-sync"
            );

            userEventProducer.publishUserRegisteredSync(event);
            log.info("User registration event published synchronously for: {}", request.getEmail());

            return ResponseEntity.ok("User registration successfully (synchronous)!");
        } catch (Exception e) {
            log.error("Error occurred while processing user registration user synchronously: {}", request.getEmail(), e);
            return ResponseEntity.internalServerError().body("Error registering user synchronously. Please try again later");
        }
    }

    @GetMapping("/analytics/stats")
    public ResponseEntity<String> getAnalyticsStats() {
        try {
            String stats = analyticsConsumer.getCurrentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting analytics stats", e);
            return ResponseEntity.internalServerError().body("Error retrieving analytics stats");
        }
    }

    @PostMapping("/test/bulk")
    public ResponseEntity<String> registerBulkUsers(@RequestParam(defaultValue = "10") int count) {
        try {
            log.info("Registering {} test users", count);

            for (int i = 1; i <= count; i++) {
                UserRegistrationRequest request = new UserRegistrationRequest();
                request.setName("Test User " + i);
                request.setEmail("test-user" + i + "@example.com");
                request.setPhone("+1234567890" + i);
                request.setCountry(i % 2 == 0 ? "US" : "ES");

                UserRegisteredEvent event = new UserRegisteredEvent(
                        UUID.randomUUID().toString(),
                        request.getName(),
                        request.getEmail(),
                        request.getPhone(),
                        request.getCountry(),
                        LocalDateTime.now(),
                        "bulk-test"
                );

                userEventProducer.publishUserRegistered(event);
                Thread.sleep(100);
            }

            return ResponseEntity.ok("Bulk user registration successfully!, " + count + " test users!");
        } catch (Exception e) {
            log.error("Error registering bulk users", e);
            return ResponseEntity.internalServerError().body("Error registering bulk users");
        }
    }
}
