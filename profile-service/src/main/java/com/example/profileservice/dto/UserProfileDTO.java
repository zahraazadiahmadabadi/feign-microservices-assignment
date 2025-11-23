package com.example.profileservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserProfileDTO {

    private Long profileId;

    private Long userId;
    private String bio;
    private String location;
    private Integer age;

    private UserDTO user;
}

