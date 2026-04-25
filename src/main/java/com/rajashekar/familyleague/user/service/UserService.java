package com.rajashekar.familyleague.user.service;

import com.rajashekar.familyleague.user.dto.RegisterRequest;
import com.rajashekar.familyleague.user.dto.UpdateProfileRequest;
import com.rajashekar.familyleague.user.dto.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse getProfile(String email);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    void softDelete(String email);
}
