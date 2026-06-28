package com.example.imagemanager.controller;

import com.example.imagemanager.dto.AuthResponse;
import com.example.imagemanager.dto.LoginRequest;
import com.example.imagemanager.dto.MessageResponse;
import com.example.imagemanager.dto.RegisterRequest;
import com.example.imagemanager.entity.User;
import com.example.imagemanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new MessageResponse("register success");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse.UserInfo me(@AuthenticationPrincipal User user) {
        return new AuthResponse.UserInfo(user.getId(), user.getUsername());
    }
}
