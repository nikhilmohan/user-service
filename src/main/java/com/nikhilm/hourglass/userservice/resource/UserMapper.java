package com.nikhilm.hourglass.userservice.resource;

import com.nikhilm.hourglass.userservice.models.UserDTO;
import com.nikhilm.hourglass.userservice.models.UserSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO userSessionToResponse(UserSession session);
}
