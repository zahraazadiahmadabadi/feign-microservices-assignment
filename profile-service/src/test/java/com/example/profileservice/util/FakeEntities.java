package com.example.profileservice.util;

import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserDTO;
import com.example.profileservice.dto.UserProfileDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.example.profileservice.common.RandomDataUtils.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FakeEntities {

    public static ProfileRequestDTO.ProfileRequestDTOBuilder profileRequestDTOBuilder() {
        return ProfileRequestDTO.builder()
                .userId(randomLong())
                .bio(randomString(100))
                .location(randomString(30))
                .age(randomAge());
    }

    public static ProfileResponseDTO.ProfileResponseDTOBuilder profileResponseDTOBuilder() {
        return ProfileResponseDTO.builder()
                .id(randomLong())
                .userId(randomLong())
                .bio(randomString(100))
                .location(randomString(30))
                .age(randomAge());
    }

    public static UserDTO.UserDTOBuilder userDTOBuilder() {
        return UserDTO.builder()
                .id(randomLong())
                .name(randomString(20))
                .email(randomEmail());
    }

    public static UserProfileDTO.UserProfileDTOBuilder userProfileDTOBuilder() {
        return UserProfileDTO.builder()
                .profileId(randomLong())
                .userId(randomLong())
                .bio(randomString(100))
                .location(randomString(30))
                .age(randomAge())
                .user(userDTOBuilder().build());
    }
}

