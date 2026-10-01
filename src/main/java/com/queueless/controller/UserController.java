package com.queueless.controller;

import com.queueless.dto.PasswordResetRequest;
import com.queueless.entity.User;
import com.queueless.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long userId,
            @RequestBody PasswordResetRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().build();
        }

        return userService.resetPassword(userId, request.getNewPassword())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}