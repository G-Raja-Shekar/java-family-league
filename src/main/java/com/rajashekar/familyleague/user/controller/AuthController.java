package com.rajashekar.familyleague.user.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.security.JwtService;
import com.rajashekar.familyleague.user.dto.LoginRequest;
import com.rajashekar.familyleague.user.dto.LoginResponse;
import com.rajashekar.familyleague.user.dto.RegisterRequest;
import com.rajashekar.familyleague.user.dto.UserResponse;
import com.rajashekar.familyleague.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        URI location = URI.create("/api/v1/users/" + user.id());
        return ResponseEntity.created(location).body(ApiResponse.ok(user));
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.issueToken(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                new LoginResponse(token, jwtService.getExpiresInSeconds())
        ));
    }
}
