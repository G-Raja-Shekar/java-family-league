package com.rajashekar.familyleague.user.mapper;

import com.rajashekar.familyleague.user.dto.UserResponse;
import com.rajashekar.familyleague.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(user.getId(), user.getEmail(),
                user.getDisplayName(), user.getAvatarUrl(), user.getRole());
    }
}
