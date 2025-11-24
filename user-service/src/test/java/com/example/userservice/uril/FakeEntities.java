package com.example.userservice.uril;

import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.dto.UserResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

import static com.example.userservice.common.RandomDataUtils.randomEmail;
import static com.example.userservice.common.RandomDataUtils.randomString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FakeEntities {

    public static UserRequestDTO.UserRequestDTOBuilder userRequestDTOBuilder() {
        return UserRequestDTO.builder()
                .email(randomEmail())
                .name(randomString(20));

    }

    public static UserResponseDTO.UserResponseDTOBuilder userResponseDTOBuilder() {
        return UserResponseDTO.builder()
                .email(randomEmail())
                .name(randomString(20))
                .id(ThreadLocalRandom.current().nextLong(1, 1000));
    }
}
