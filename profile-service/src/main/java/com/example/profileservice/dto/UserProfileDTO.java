package com.example.profileservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Long profileId;

    private Long userId;
    private String bio;
    private String location;
    private Integer age;

    private UserDTO user;
}

