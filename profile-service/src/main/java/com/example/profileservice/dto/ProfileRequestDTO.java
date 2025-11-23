package com.example.profileservice.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileRequestDTO {

    @NotNull(message = "User id is required")
    private Long userId;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    private String location;

    @Min(value = 0, message = "Age must be positive")
    private Integer age;
}

