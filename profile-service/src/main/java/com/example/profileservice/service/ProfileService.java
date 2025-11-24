package com.example.profileservice.service;

import com.example.profileservice.client.UserClient;
import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserDTO;
import com.example.profileservice.dto.UserProfileDTO;
import com.example.profileservice.entity.Profile;
import com.example.profileservice.exception.ProfileNotFoundException;
import com.example.profileservice.exception.UserNotFoundException;
import com.example.profileservice.mapper.ProfileMapper;
import com.example.profileservice.repository.ProfileRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final UserClient userClient;

    @Transactional
    @CircuitBreaker(name = "userService", fallbackMethod = "createProfileFallback")
    @Retry(name = "userService")
    public ProfileResponseDTO create(ProfileRequestDTO dto) {
        Long userId = dto.getUserId();

        try {
            userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(userId);
        }

        Profile profile = profileMapper.toEntity(dto);
        Profile saved = profileRepository.save(profile);
        return profileMapper.toDto(saved);
    }

    private ProfileResponseDTO createProfileFallback(ProfileRequestDTO profileRequestDTO, Throwable ex) {

        if (ex instanceof UserNotFoundException) {
            throw (UserNotFoundException) ex;
        }

        throw new RuntimeException("User service is not available right now. Please try again later.", ex);
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserProfileFallback")
    @Retry(name = "userService")
    public UserProfileDTO getUserProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException(profileId));

        UserDTO user;
        try {
            user = userClient.getUserById(profile.getUserId());
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(profile.getUserId());
        }

        return profileMapper.toUserProfileDto(profile, user);
    }

    private UserProfileDTO getUserProfileFallback(Long profileId, Throwable ex) {

        if (ex instanceof ProfileNotFoundException) {
            throw (ProfileNotFoundException) ex;
        }
        if (ex instanceof UserNotFoundException) {
            throw (UserNotFoundException) ex;
        }

        throw new RuntimeException("User service is not available right now. Cannot load user profile.", ex);
    }
}
