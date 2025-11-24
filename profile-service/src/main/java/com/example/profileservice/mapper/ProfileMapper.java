package com.example.profileservice.mapper;


import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserDTO;
import com.example.profileservice.dto.UserProfileDTO;
import com.example.profileservice.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Profile toEntity(ProfileRequestDTO dto);

    ProfileResponseDTO toDto(Profile profile);

    @Mapping(target = "profileId", source = "profile.id")
    @Mapping(target = "userId", source = "profile.userId")
    @Mapping(target = "bio", source = "profile.bio")
    @Mapping(target = "location", source = "profile.location")
    @Mapping(target = "age", source = "profile.age")
    @Mapping(target = "user", source = "user")
    UserProfileDTO toUserProfileDto(Profile profile, UserDTO user);
}

