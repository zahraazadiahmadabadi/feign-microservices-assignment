package com.example.profileservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponseDTO {

    private Long id;
    private Long userId;
    private String bio;
    private String location;
    private Integer age;
}
