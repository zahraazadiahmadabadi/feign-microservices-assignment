package com.example.profileservice.controller;


import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserProfileDTO;
import com.example.profileservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "Profile management APIs")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create profile for existing user",
            description = "Creates a profile after verifying the user via User Service (Feign)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ProfileResponseDTO createProfile(
            @RequestBody @Valid
            @Parameter(description = "Profile data including userId")
            ProfileRequestDTO request
    ) {
        return profileService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get profile with user data",
            description = "Returns profile information combined with user data"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Combined user + profile returned"),
            @ApiResponse(responseCode = "404", description = "Profile or user not found")
    })
    public UserProfileDTO getProfileWithUser(
            @PathVariable("id")
            Long id
    ) {
        return profileService.getUserProfile(id);
    }
}

