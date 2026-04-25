package com.rajashekar.familyleague.user.controller;

import com.rajashekar.familyleague.common.response.ApiResponse;
import com.rajashekar.familyleague.common.security.CurrentUser;
import com.rajashekar.familyleague.user.dto.UpdateProfileRequest;
import com.rajashekar.familyleague.user.dto.UserResponse;
import com.rajashekar.familyleague.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    ResponseEntity<ApiResponse<UserResponse>> getProfile(@CurrentUser UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(principal.getUsername())));
    }

    @PatchMapping("/me")
    ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.updateProfile(principal.getUsername(), request)
        ));
    }
}
