package com.rajashekar.familyleague.user.mapper;

import com.rajashekar.familyleague.user.dto.UserResponse;
import com.rajashekar.familyleague.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
